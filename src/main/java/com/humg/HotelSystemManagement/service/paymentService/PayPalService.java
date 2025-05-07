package com.humg.HotelSystemManagement.service.paymentService;

import com.humg.HotelSystemManagement.configuration.payment.PayPalConfig;
import com.humg.HotelSystemManagement.dto.request.payment.PayPalOrderRequest;
import com.humg.HotelSystemManagement.dto.request.payment.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.entity.booking.PaymentBill;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import com.humg.HotelSystemManagement.repository.booking.PaymentBillRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayPalService {
    APIContext apiContext;
    PayPalConfig payPalConfig;
    //PaymentBill paymentBill;
    PaymentBillRepository paymentBillRepository;
    BookingBillRepository bookingBillRepository;

    public String createOrder(PayPalOrderRequest request) throws PayPalRESTException {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var bookingBill = bookingBillRepository.findById(request.getBookingBillId())
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        //Tiền VND
        var grandTotal = bookingBill.getGrandTotal();

        //Đổi sang tiền đô
        double exchangeToDolar = grandTotal / 26000;
        BigDecimal usdTotal = new BigDecimal(exchangeToDolar).setScale(2, RoundingMode.HALF_UP); //Làm tròn lên 2 số thập phân

        //Tạo đối tượng lưu trữ số tiền cho giao dịch
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(usdTotal.toPlainString());

        Transaction transaction = new Transaction();
        transaction.setDescription("Payment for booking bill id: " + request.getBookingBillId());
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("PayPal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(payPalConfig.getCancelUrl());
        redirectUrls.setReturnUrl(payPalConfig.getSuccessUrl());
        payment.setRedirectUrls(redirectUrls);

        //Tạo order Paypal
        Payment createPayment = payment.create(apiContext);

        //Lấy approval_url
        String approvalUrl = createPayment.getLinks().stream()
                .filter(links -> "approval_url".equals(links.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_CREATE_FAILED));

        log.info("Created PayPal order with approval URL: {}", approvalUrl);
        return approvalUrl;
    }

    public Payment executeOrder(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }

}