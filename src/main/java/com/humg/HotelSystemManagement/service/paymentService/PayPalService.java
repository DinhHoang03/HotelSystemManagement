package com.humg.HotelSystemManagement.service.paymentService;

import com.humg.HotelSystemManagement.entity.booking.Payment;
import com.paypal.api.payments.Amount;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayPalService {
    APIContext apiContext;

    public Payment createOrder(Double total,
                               String currency,
                               String method,
                               String intent,
                               String description,
                               String cancelUrl,
                               String successUrl) throws PayPalRESTException {
        //Tạo đối tượng lưu trữ số tiền cho giao dịch
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal()
    }

}