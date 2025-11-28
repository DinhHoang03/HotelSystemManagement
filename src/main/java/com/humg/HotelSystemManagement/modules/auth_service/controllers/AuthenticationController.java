package com.humg.HotelSystemManagement.modules.auth_service.controllers;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.AuthenticationRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.IntrospectRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.LogOutRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.RefreshRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.UserCreationRequest;
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

    // --- 1. ĐĂNG NHẬP (Nới lỏng) ---
    @PostMapping("/login")
    APIResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        var authResult = authenticationService.authenticate(request);

        // Access Token
        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResult.getAccessToken())
                .httpOnly(true)
                .secure(false)       // <--- SỬA: False để chạy HTTP
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Lax")     // <--- SỬA: Lax cho dễ thở
                .build();

        // Refresh Token
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResult.getRefreshToken())
                .httpOnly(true)
                .secure(false)       // <--- SỬA
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")     // <--- SỬA
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return APIResponse.<AuthenticationResponse>builder()
                .result(authResult)
                .message("Login successful")
                .build();
    }

    @PostMapping("/introspect")
    APIResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return APIResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    // --- 2. LÀM MỚI TOKEN (Nới lỏng) ---
    @PostMapping("/refresh")
    APIResponse<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {

        // B1: Lấy refresh_token từ Cookie
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // B2: Nếu không có Cookie -> Lỗi 401
        if (refreshToken == null) {
            throw new AppException(AppErrorCode.UNAUTHENTICATED);
        }

        // B3: Gọi Service (Bọc lại vào RefreshRequest vì Service đang yêu cầu Object này)
        var result = authenticationService.refreshToken(
                RefreshRequest.builder().token(refreshToken).build()
        );

        // B4: Set lại Cookie mới (Access Token mới)
        ResponseCookie accessCookie = ResponseCookie.from("access_token", result.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Lax")
                .build();

        // Nếu Service trả về cả Refresh Token mới (Rotating Refresh Token) thì set luôn
        if (result.getRefreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
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

    // --- 3. ĐĂNG XUẤT (Nới lỏng) ---
    @PostMapping("/logout")
    APIResponse<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) accessToken = cookie.getValue();
                if ("refresh_token".equals(cookie.getName())) refreshToken = cookie.getValue();
            }
        }

        if (accessToken != null) {
            try { authenticationService.logout(accessToken); } catch (Exception e) {}
        }
        if (refreshToken != null) {
            try { authenticationService.logout(refreshToken); } catch (Exception e) {}
        }

        // Xóa Cookie (Cấu hình phải khớp với lúc tạo)
        ResponseCookie deleteAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)       // <--- SỬA
                .path("/")
                .maxAge(0)
                .sameSite("Lax")     // <--- SỬA
                .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)       // <--- SỬA
                .path("/")
                .maxAge(0)
                .sameSite("Lax")     // <--- SỬA
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        return APIResponse.builder().message("Logout success").build();
    }

    @PostMapping("/register")
    APIResponse<?> register(@Valid @RequestBody UserCreationRequest request) {
        authenticationService.registerStep1(request);
        return APIResponse.builder()
                .message("Register initiated. Please check your email for OTP.")
                .build();
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