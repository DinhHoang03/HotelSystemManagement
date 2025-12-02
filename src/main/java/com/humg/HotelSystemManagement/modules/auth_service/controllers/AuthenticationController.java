package com.humg.HotelSystemManagement.modules.auth_service.controllers;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.AuthenticationRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.IntrospectRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.RefreshRequest;
import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.OTPRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.AuthenticationResponse;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.IntrospectResponse;
import com.humg.HotelSystemManagement.modules.auth_service.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    // Inject giá trị từ YAML (Đơn vị: PHÚT)
    @NonFinal @Value("${jwt.valid-duration}")
    protected long VALID_DURATION_MINUTES;

    @NonFinal @Value("${jwt.refreshable-duration}")
    protected long REFRESH_STANDARD_MINUTES;

    @NonFinal @Value("${jwt.remember-me-duration}")
    protected long REMEMBER_ME_MINUTES;

    @PostMapping("/login")
    APIResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        var authResult = authenticationService.authenticate(request);

        // 1. Access Cookie (Đổi phút -> giây)
        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResult.getAccessToken())
                .httpOnly(true)
                .secure(false) // Localhost: false
                .path("/")
                .maxAge(VALID_DURATION_MINUTES * 60)
                .sameSite("Lax")
                .build();

        // 2. Refresh Cookie (Đổi phút -> giây)
        long refreshMaxAgeSeconds;
        if (request.isRememberMe()) {
            refreshMaxAgeSeconds = REMEMBER_ME_MINUTES * 60;
        } else {
            refreshMaxAgeSeconds = REFRESH_STANDARD_MINUTES * 60;
        }

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResult.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshMaxAgeSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return APIResponse.<AuthenticationResponse>builder()
                .result(authResult)
                .message("Login successful")
                .build();
    }

    @PostMapping("/refresh")
    APIResponse<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {

        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        var result = authenticationService.refreshToken(
                RefreshRequest.builder().token(refreshToken).build()
        );

        // Cấp lại Access Cookie
        ResponseCookie accessCookie = ResponseCookie.from("access_token", result.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(VALID_DURATION_MINUTES * 60)
                .sameSite("Lax")
                .build();

        // Cấp lại Refresh Cookie (Reset về Standard)
        if (result.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(REFRESH_STANDARD_MINUTES * 60)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return APIResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Refresh token successful")
                .build();
    }

    @PostMapping("/logout")
    APIResponse<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) accessToken = cookie.getValue();
                if ("refresh_token".equals(cookie.getName())) refreshToken = cookie.getValue();
            }
        }

        if (accessToken != null) try { authenticationService.logout(accessToken); } catch (Exception e) {}
        if (refreshToken != null) try { authenticationService.logout(refreshToken); } catch (Exception e) {}

        ResponseCookie deleteAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        return APIResponse.builder().message("Logout success").build();
    }

    @PostMapping("/introspect")
    APIResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return APIResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/register")
    APIResponse<?> register(@Valid @RequestBody UserCreationRequest request) {
        authenticationService.registerStep1(request);
        return APIResponse.builder().message("Register initiated. Check email for OTP.").build();
    }

    @PostMapping("/verify-registration")
    APIResponse<AuthenticationResponse> verifyRegistration(@Valid @RequestBody OTPRequest request) {
        var authResponse = authenticationService.registerStep2_Verify(request);
        return APIResponse.<AuthenticationResponse>builder()
                .result(authResponse)
                .message("Registration successful & Logged in!")
                .build();
    }
}