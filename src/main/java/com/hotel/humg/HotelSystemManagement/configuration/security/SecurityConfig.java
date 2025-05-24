package com.hotel.humg.HotelSystemManagement.configuration.security;

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
    private String signerKey;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/introspect",
            "/customer/register",
            "/employee/register",
            "/zalopay/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/offer/list/**",
            "/customer-dashboard.html",
            "/employee-dashboard.html",
            "/admin-dashboard.html",
            "/registration-success.html",
            "/forgot-password.html",
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
            "/*.svg",
            "/email/**"
    };

    private static final String[] ADMIN_ENDPOINTS = {"/admin/**", "/room/create"};

    @Bean
    public PasswordEncoder bcryptPasswordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(authorize -> {
                    // Debug: Log các endpoints được cấu hình
                    System.out.println("=== CONFIGURING SECURITY ===");
                    System.out.println("PUBLIC_ENDPOINTS: " + String.join(", ", PUBLIC_ENDPOINTS));
                    System.out.println("ADMIN_ENDPOINTS: " + String.join(", ", ADMIN_ENDPOINTS));

                    authorize
                            // Thử chỉ định rõ ràng cho email endpoints
                            .requestMatchers(HttpMethod.POST, "/email/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/email/**").permitAll()
                            // Các public endpoints khác
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                            .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
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

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}