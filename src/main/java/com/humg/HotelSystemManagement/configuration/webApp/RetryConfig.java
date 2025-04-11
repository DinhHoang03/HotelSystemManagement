package com.humg.HotelSystemManagement.configuration.webApp;

import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

@Configuration
@EnableRetry
public class RetryConfig {
    //Kích hoạt retry để kết nối lại tới database
}
