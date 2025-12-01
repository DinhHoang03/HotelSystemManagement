package com.humg.HotelSystemManagement.modules.payment_service.controllers;

import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.payment_service.services.ZaloPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/zalopay")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayController {
    ZaloPayService zaloPayService;

    @PostMapping("/order")
    @PreAuthorize("hasAuthority('PAYMENT_EXECUTE')")
    APIResponse<String> createOrder(@RequestBody ZaloPayOrderRequest request) throws IOException {
        var result = zaloPayService.createPayment(request);
        return APIResponse.<String>builder()
                .result(result)
                .message("Create order success")
                .build();
    }

    // API này để ZaloPay gọi (Callback)
    @PostMapping("/callback")
    public Map<String, Object> callback(@RequestBody String jsonStr) {
        return zaloPayService.doCallback(jsonStr).toMap();
    }

    // API này để Frontend (React) gọi khi khách quay về web
    @PostMapping("/check-status/{appTransId}")
    public Map<String, Object> checkStatus(@PathVariable String appTransId) {
        return zaloPayService.checkOrderStatus(appTransId);
    }
}