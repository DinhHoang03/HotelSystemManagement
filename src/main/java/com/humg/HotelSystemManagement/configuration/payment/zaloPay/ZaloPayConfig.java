package com.humg.HotelSystemManagement.configuration.payment.zaloPay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {

    @Value("${zalo-pay.app-id}")
    private int appId;

    @Value("${zalo-pay.key1}")
    private String key1;

    @Value("${zalo-pay.key2}")
    private String key2;

    @Value("${zalo-pay.create-order-url}")
    private String createOrderUrl;

    @Bean
    public ZaloPayContext zaloPayContext() {
        return new ZaloPayContext(appId, key1, key2, createOrderUrl);
    }
}
