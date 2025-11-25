package com.humg.HotelSystemManagement.modules.auth_service.configs;

import com.humg.HotelSystemManagement.modules.auth_service.filters.JwtCookieFilter; // Import Filter của bạn
import lombok.RequiredArgsConstructor; // Dùng cái này để tự Inject Filter
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor // <--- Tự động Inject các field final (jwtCookieFilter)
public class SecurityConfig {

    // Inject Filter vào đây (Không cần @Autowired vì đã có @RequiredArgsConstructor)
    private final JwtCookieFilter jwtCookieFilter;

    // Các endpoint public
    private static final String[] PUBLIC_API_ENDPOINTS = {
            "/auth/**",
            "/customer/register",
            "/employee/register",
            "/email/**",
            "/zalopay/**",
            "/offer/list/**"
    };

    @Bean
    public PasswordEncoder bcryptPasswordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                // QUAN TRỌNG: Thêm Filter Cookie vào trước
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_API_ENDPOINTS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()) // Nó sẽ tự tìm Bean JwtDecoder bên JwtConfig
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

        // QUAN TRỌNG: Phải liệt kê chính xác, không được dùng "*"
        // Bạn đang chạy frontend ở port nào? Kiểm tra thanh địa chỉ trình duyệt.
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173"   // Port của VS Code Live Server
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ NHẬN COOKIE
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}