package com.humg.HotelSystemManagement.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    //Kích hoạt retry để kết nối lại tới database
}
