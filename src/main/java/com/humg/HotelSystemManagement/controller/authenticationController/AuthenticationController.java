package com.humg.HotelSystemManagement.controller.authenticationController;

import com.humg.HotelSystemManagement.dto.request.security.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.LogOutRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.RefreshRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.security.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.security.jwt.IntrospectResponse;
import com.humg.HotelSystemManagement.service.SecurityService.AuthenticationService;
import com.humg.HotelSystemManagement.utils.CookieUtils;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    CookieUtils cookieUtils;

    @PostMapping("/login")
    APIResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        var result = authenticationService.authenticate(request, response);
        return APIResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    APIResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request, HttpServletRequest httpRequest) throws ParseException, JOSEException {
        String token = request.getToken();
        if(token == null) token = cookieUtils.getTokenFromCookie(httpRequest);

        if(token == null) {
            return APIResponse.<IntrospectResponse>builder()
                    .result(IntrospectResponse.builder().valid(false).build())
                    .message("Token not found")
                    .build();
        }

        var result = authenticationService.introspect(new IntrospectRequest(token));
        return APIResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    APIResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request,  HttpServletResponse response)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request, response);
        return APIResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    APIResponse<?> logout(@RequestBody LogOutRequest request, HttpServletResponse response, HttpServletRequest httpRequest) throws ParseException, JOSEException {
        String token = request.getToken();
        if(token == null) token = cookieUtils.getTokenFromCookie(httpRequest);

        if(token == null) {
            return APIResponse.builder()
                    .message("Token not found")
                    .build();
        }

        authenticationService.logout(request, response);
        return APIResponse.builder()
                .message("Logout successfully")
                .build();
    }
}
