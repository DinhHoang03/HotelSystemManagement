package com.humg.HotelSystemManagement.service.paymentService;

import com.humg.HotelSystemManagement.configuration.payment.zaloPay.ZaloPayContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayService {
    ZaloPayContext zaloPayContext;
    HttpClient httpClient = HttpClient.newHttpClient();

    public String createPayment(Long total, String description) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String timePrefix = now.format(DateTimeFormatter.ofPattern("yyMMdd"));

        Map<String, String> orderData = new HashMap<>();
        orderData.put("app_id", String.valueOf(zaloPayContext.getAppId()));
        orderData.put("app_user", "user");
        orderData.put("app_trans_id", String.valueOf(now));
    }
}
