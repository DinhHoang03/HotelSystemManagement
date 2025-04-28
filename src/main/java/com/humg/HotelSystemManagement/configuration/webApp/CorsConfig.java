package com.humg.HotelSystemManagement.configuration.webApp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //Áp dụng cho tất cả các endpoint
                .allowedOrigins("https://localhost:8443", "http://localhost:8443", "http://localhost:5050") //Cho phép cổng font-end hoạt động ở cổng 5050
                .allowedMethods("GET", "POST", "PUT", "DELETE") //Các phương thức HTTP được phép thực thi
                .allowedHeaders("*") //Cho pheps tất cả header
                .allowCredentials(true); //Cho phép gửi cookie hoặc thông tin xác thực
    }
}
