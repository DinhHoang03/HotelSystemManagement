package com.hotel.humg.HotelSystemManagement.controller.PaymentController;

import com.hotel.humg.HotelSystemManagement.dto.request.payment.PayPalOrderRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.APIResponse;
import com.hotel.humg.HotelSystemManagement.service.paymentService.PayPalService;
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
