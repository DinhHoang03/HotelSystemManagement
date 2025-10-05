package com.humg.HotelSystemManagement.modules.payment_service.controllers;

import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.payment_service.services.ZaloPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/zalopay")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZaloPayController {
    ZaloPayService zaloPayService;

    @PostMapping("/order")
    APIResponse<String> createOrder(@RequestBody ZaloPayOrderRequest request) throws IOException {
        var result = zaloPayService.createPayment(request);
        return APIResponse.<String>builder()
                .result(result)
                .message("Create order success")
                .build();
    }
}
