package com.humg.HotelSystemManagement.modules.payment_service.controllers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.SuccessfulPaymentResponse;
import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.PayPalOrderRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.payment_service.services.PayPalService;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/paypal")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayPalController {
    PayPalService payPalService;

    @PostMapping("/order")
    @PreAuthorize("hasAuthority('PAYMENT_EXECUTE')")
    public APIResponse<String> createOrder(@RequestBody PayPalOrderRequest request) throws PayPalRESTException {
        return APIResponse.<String>builder()
                .result(payPalService.createOrder(request))
                .message("Create order successfully")
                .build();
    }

    // API 2: Xác nhận thanh toán (User quay lại từ PayPal) - CẦN THÊM CÁI NÀY
    @PostMapping("/success")
    // @PreAuthorize(...) // Tùy chọn: Có thể cần quyền user hoặc public tùy logic bảo mật của bạn
    public APIResponse<SuccessfulPaymentResponse> successPay(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("bookingId") String bookingId
    ) {
        // Gọi xuống service để thực hiện chốt đơn và lưu DB
        SuccessfulPaymentResponse response = payPalService.executeOrder(paymentId, payerId, bookingId);

        return APIResponse.<SuccessfulPaymentResponse>builder()
                .result(response)
                .message("Payment executed successfully")
                .build();
    }
}
