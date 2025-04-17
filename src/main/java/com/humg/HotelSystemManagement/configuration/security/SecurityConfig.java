package com.humg.HotelSystemManagement.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.signerKey}")
    private String signerKey; //SECRET KEY(Không được phép để lộ!)

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/introspect",
            "/customer/register",
            "/employee/register",
            "/zalopay",
            "/css/**",
            "/js/**",
            "/images/**",
            "/offer/list/**",
            "/customer-dashboard.html",
            "/employee-dashboard.html",
            "/admin-dashboard.html",
            "/registration-success.html",
            "/my-bills.html",
            "/my-bookings.html",
            "/register.html",
            "/login.html",
            "/*.html",
            "/*.js",
            "/*.css",
            "/*.png",
            "/*.jpg",
            "/*.jpeg",
            "/*.gif",
            "/*.svg"
    };//Các end-point được public mà không cần phải có sự can thiệp từ spring security

    /*
    private static final String[] ADMIN_ENDPOINTS = {
            "/admin/get-customer/{customerId}",
            "/admin/get-customers/list",
            "/admin/get-employee/{employeeId}",
            "/admin/get-employees/list"

    };//Các end-point được chỉ định để phân quyền riêng cho role admin!
    */

    @Bean
    public PasswordEncoder bcryptPasswordEncoder(){ //Hàm nâng cấp độ khó của mã hóa mật khẩu(Sử dụng thuật toán BCrypt)
        return new BCryptPasswordEncoder(10); //Độ khó mặc định của hệ thống
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{ //Hàm phân lọc end-point để thực hiện phân quyền trong hệ thống theo role
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new JWTAuthenticationEntryPoint())
            );

        return http.build();
    }

    // Hàm tạo JwtDecoder để xác minh và giải mã JWT thành đối tượng Jwt, thường dùng trong xác thực token hoặc OAuth2.
    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512"); // Chuyển khóa bí mật (signerKey) thành đối tượng SecretKeySpec để sử dụng với thuật toán HMAC-SHA512 (đối xứng).
        return NimbusJwtDecoder // Lớp NimbusJwtDecoder dùng để xác minh chữ ký và giải mã JWT.
                .withSecretKey(secretKeySpec) // Thiết lập khóa bí mật (secretKeySpec) để NimbusJwtDecoder tính toán lại chữ ký từ header và payload của token nhằm so sánh với chữ ký hiện có.
                .macAlgorithm(MacAlgorithm.HS512) // Chỉ định thuật toán HMAC-SHA512 để tính toán và xác minh chữ ký của JWT.
                .build(); // Tạo và trả về đối tượng JwtDecoder đã được cấu hình.
    }

    // Hàm tạo JwtAuthenticationConverter để chuyển đổi JWT thành đối tượng Authentication trong Spring Security, dùng trong xác thực và phân quyền.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Tạo JwtGrantedAuthoritiesConverter để trích xuất quyền (authorities) từ JWT.
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Thiết lập tiền tố "ROLE_" cho mỗi quyền, ví dụ: "read" thành "ROLE_read".
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        // Chỉ định claim "scope" trong JWT là nơi chứa danh sách quyền (thay vì mặc định "scope" hoặc "scp").
        //jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        // Tạo JwtAuthenticationConverter để chuyển đổi toàn bộ JWT thành Authentication.
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // Gắn JwtGrantedAuthoritiesConverter đã cấu hình để xử lý quyền từ JWT.
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        // Trả về JwtAuthenticationConverter làm bean để Spring Security sử dụng.
        return jwtAuthenticationConverter;
    }
}
