package com.humg.HotelSystemManagement.modules.auth_service.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Tìm Token trong Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Nếu không thấy trong Cookie, thử tìm trong Header (Authorization: Bearer ...)
        // (Để support cả Postman khi test bằng Header)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 3. Nếu có token và chưa được xác thực trong Context
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Giải mã Token (Verify chữ ký, hết hạn...)
                Jwt jwt = jwtDecoder.decode(token);

                // Chuyển đổi JWT thành Authentication object (Gồm cả Role/Permission)
                // Bước này cực quan trọng để @PreAuthorize hoạt động
                var authentication = (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

                // Nạp vào Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {}", authentication.getName());
                log.debug("Authorities: {}", authentication.getAuthorities());

            } catch (Exception e) {
                // Nếu Token lỗi (hết hạn, sai chữ ký), ta chỉ log và bỏ qua.
                // Request sẽ đi tiếp dưới dạng "Anonymous" (Vô danh) -> Gặp Controller sẽ bị 401
                log.error("Failed to authenticate JWT from Cookie: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}