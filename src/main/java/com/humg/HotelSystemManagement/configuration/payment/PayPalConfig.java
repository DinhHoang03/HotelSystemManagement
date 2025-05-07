package com.humg.HotelSystemManagement.configuration.payment;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.error-url}")
    private String errorUrl;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    @Bean
    public APIContext apiContext() {
        return new APIContext(clientId, clientSecret, mode);
    }
}
