package com.humg.HotelSystemManagement.configuration.payment;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZaloPayConfig {

    @Value("${zalo-pay.app-id}")
    private Integer appId;

    @Value("${zalo-pay.key1}")
    private String key1;

    @Value("${zalo-pay.key2}")
    private String key2;

    @Value("${zalo-pay.create-order-url}")
    private String createOrderUrl;

    @Value("${zalo-pay.callback-url}")
    private String callbackUrl;

    @Value("${zalo-pay.redirect-url}")
    private String redirectUrl;

    @PostConstruct
    public void init() {
        System.out.println("ZaloPay Configuration:");
        System.out.println("App ID: " + appId);
        System.out.println("Create Order URL: " + createOrderUrl);
        System.out.println("Redirect URL: " + redirectUrl);
        System.out.println("Callback URL: " + callbackUrl);
    }

    public Integer getAppId() {
        return appId;
    }

    public String getKey1() {
        return key1;
    }

    public String getKey2() {
        return key2;
    }

    public String getCreateOrderUrl() {
        return createOrderUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
