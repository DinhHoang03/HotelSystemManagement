package com.humg.HotelSystemManagement.modules.payment_service.services;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingService;
import com.humg.HotelSystemManagement.modules.payment_service.configs.PayPalConfig;
import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.PayPalOrderRequest;
import com.humg.HotelSystemManagement.utils.enums.PaymentMethod;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayPalService {
    APIContext apiContext;
    PayPalConfig payPalConfig;
    BookingRepository bookingRepository; // Dùng Booking thay vì BookingBill
    BookingService bookingService;

    public String createOrder(PayPalOrderRequest request) throws PayPalRESTException {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        // Lấy Booking trực tiếp từ request (request nên gửi bookingId)
        String bookingId = request.getBookingBillId(); // Mapping field này thành bookingId
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        // Đổi tiền VND -> USD
        double exchangeToDolar = booking.getGrandTotal() / 26000.0;
        BigDecimal usdTotal = new BigDecimal(exchangeToDolar).setScale(2, RoundingMode.HALF_UP);

        // Cấu hình giao dịch PayPal
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(usdTotal.toPlainString());

        Transaction transaction = new Transaction();
        transaction.setDescription("Payment for Booking #" + booking.getBookingId());
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(payPalConfig.getCancelUrl());
        redirectUrls.setReturnUrl(payPalConfig.getSuccessUrl());
        payment.setRedirectUrls(redirectUrls);

        Payment createPayment = payment.create(apiContext);

        return createPayment.getLinks().stream()
                .filter(links -> "approval_url".equals(links.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_CREATE_FAILED));
    }

    public Payment executeOrder(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        // Thực thi thanh toán
        Payment executedPayment = payment.execute(apiContext, paymentExecution);

        // Lấy thông tin từ kết quả trả về
        Transaction transaction = executedPayment.getTransactions().get(0);
        String description = transaction.getDescription(); // "Payment for Booking #UUID..."
        String bookingId = description.replace("Payment for Booking #", "").trim();

        // Lấy số tiền thực trả
        String totalAmountUSD = transaction.getRelatedResources().get(0).getSale().getAmount().getTotal();
        BigDecimal totalAmountVND = new BigDecimal(totalAmountUSD).multiply(new BigDecimal(26000));

        // Gọi BookingService để chốt đơn (Cập nhật trạng thái và lưu PaymentBill)
        bookingService.processSuccessfulPayment(
                bookingId,
                executedPayment.getId(),
                totalAmountVND.longValue(),
                PaymentMethod.PAYPAL
        );

        return executedPayment;
    }
}