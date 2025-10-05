package com.humg.HotelSystemManagement.modules.payment_service.controllers;

import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.PayPalOrderRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.payment_service.services.PayPalService;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paypal")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayPalController {
    PayPalService payPalService;

    @PostMapping("/order")
    public APIResponse<String> createOrder(@RequestBody PayPalOrderRequest request) throws PayPalRESTException {
        return APIResponse.<String>builder()
                .result(payPalService.createOrder(request))
                .message("Create order successfully")
                .build();
    }
}
