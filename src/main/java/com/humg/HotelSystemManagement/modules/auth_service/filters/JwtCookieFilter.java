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

        // Log URL đang được gọi để dễ theo dõi
        log.info(">>> FILTER START: Processing request {} {}", request.getMethod(), request.getRequestURI());

        String token = null;

        // 1. Tìm Token trong Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    log.info(">>> Found 'access_token' in Cookie.");
                    break;
                }
            }
        } else {
            log.info(">>> No cookies found in request.");
        }

        // 2. Nếu không thấy trong Cookie, thử tìm trong Header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.info(">>> Found 'access_token' in Authorization Header.");
            }
        }

        if (token == null) {
            log.warn(">>> Token is NULL. Request will proceed as Anonymous.");
        }

        // 3. Nếu có token và chưa được xác thực trong Context
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Giải mã Token
                Jwt jwt = jwtDecoder.decode(token);
                log.info(">>> JWT Decoded Successfully. Subject (User): {}", jwt.getSubject());

                // Chuyển đổi JWT thành Authentication object
                var authentication = (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);

                // --- LOG QUAN TRỌNG NHẤT: KIỂM TRA QUYỀN ---
                log.info(">>> Authorities extracted from Token: {}", authentication.getAuthorities());

                // Nạp vào Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info(">>> SecurityContext updated successfully.");

            } catch (Exception e) {
                log.error(">>> FAILED to authenticate JWT: {}", e.getMessage());
                // Không throw exception ở đây để filter chain tiếp tục (Spring Security sẽ handle lỗi 401 sau)
                SecurityContextHolder.clearContext();
            }
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info(">>> SecurityContext already contains authentication: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        }

        filterChain.doFilter(request, response);
    }
}