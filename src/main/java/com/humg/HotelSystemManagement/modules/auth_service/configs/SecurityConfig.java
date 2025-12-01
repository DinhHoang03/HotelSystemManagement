package com.humg.HotelSystemManagement.modules.auth_service.configs;

import com.humg.HotelSystemManagement.modules.auth_service.filters.JwtCookieFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtCookieFilter jwtCookieFilter;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    // --- CẬP NHẬT DANH SÁCH PUBLIC ---
    private static final String[] PUBLIC_API_ENDPOINTS = {
            // 1. Auth & User
            "/auth/**",

            // 2. Room Service (Cho khách xem phòng)
            "/room/list/**",        // Xem danh sách phòng
            "/room/info/**",        // Xem chi tiết 1 phòng (MỚI)

            "/rooms/list/**",        // Xem danh sách đặt phòng
            "/rooms/info/**",        // Xem chi tiết 1 phòng đã đặt (MỚI)

            "/items/list/**",        // Xem danh sách các dịch vụ đã đặt
            "/items/info/**",        // Xem chi tiết 1 dịch vụ đã đặt

            "/booking/list/**",        // Xem danh sách phòng
            "/booking/info/**",        // Xem chi tiết 1 lịch đặt

            // 3. Room Type Service (Cho khách xem loại phòng)
            "/type/list/**",        // Xem danh sách loại phòng (MỚI)
            "/type/get-all/list",   // Path cũ (giữ lại nếu còn dùng)
            "/type/info/**",        // Xem chi tiết loại phòng (MỚI)

            // 4. Hotel Offers (Menu dịch vụ)
            "/api/v1/offers/list",
            "/api/v1/offers/category/**",

            "/otp/**",

            // 5. Payment Callback (ZaloPay gọi ngược lại)
            "/zalopay/callback/**",
            "/zalopay/check-status/**",

            // 6. Upload file (Nếu muốn public ảnh thì mở, không thì thôi)
            "/api/v1/upload/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép các API trong danh sách Public truy cập thoải mái
                        .requestMatchers(PUBLIC_API_ENDPOINTS).permitAll()
                        // Các API còn lại bắt buộc phải có Token (Authenticated)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(new JWTAuthenticationEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://127.0.0.1:5173",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}