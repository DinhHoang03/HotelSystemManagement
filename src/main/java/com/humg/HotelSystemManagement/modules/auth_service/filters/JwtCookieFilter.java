package com.humg.HotelSystemManagement.modules.auth_service.filters;

import com.humg.HotelSystemManagement.modules.redis_service.services.ExTokenHandleService;
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
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ExTokenHandleService exTokenHandleService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Tìm Token trong Cookie (Lưu ý: Tìm "access_token" cho khớp với Controller)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break; // Tìm thấy rồi thì dừng loop
                }
            }
        }

        // 2. Nếu có Token và chưa xác thực trong SecurityContext
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Giải mã Token (Validate chữ ký và thời gian hết hạn)
                Jwt jwt = jwtDecoder.decode(token);

                // [QUAN TRỌNG] Kiểm tra xem Token có nằm trong Blacklist (đã Logout) không?
                if (!exTokenHandleService.isTokenBlackListed(jwt.getId())) {

                    // Token sạch -> Convert sang Authentication và set vào Context
                    JwtAuthenticationToken authentication = (JwtAuthenticationToken) jwtAuthenticationConverter.convert(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    // Token hợp lệ về mặt chữ ký nhưng đã bị người dùng Logout
                    log.warn("Token đã bị từ chối do nằm trong Blacklist (JTI: {})", jwt.getId());
                }

            } catch (JwtException e) {
                // Token lỗi, hết hạn hoặc giả mạo -> Bỏ qua, coi như người dùng chưa đăng nhập
                // Spring Security sẽ tự chặn ở các Filter phía sau nếu API yêu cầu quyền
            }
        }

        // 3. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}