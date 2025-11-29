package com.humg.HotelSystemManagement.modules.payment_service.services;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.SuccessfulPaymentResponse;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingService;
import com.humg.HotelSystemManagement.modules.email_service.services.EmailService;
import com.humg.HotelSystemManagement.modules.payment_service.configs.PayPalConfig;
import com.humg.HotelSystemManagement.modules.payment_service.resources.requests.PayPalOrderRequest;
import com.humg.HotelSystemManagement.utils.enums.PaymentMethod;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
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
    BookingRepository bookingRepository;
    BookingService bookingService;
    EmailService emailService; // 1. Thêm EmailService để gửi mail giống ZaloPay

    // Define tỷ giá
    private static final double VND_TO_USD_RATE = 26000.0;

    public String createOrder(PayPalOrderRequest request) throws PayPalRESTException {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        // Lấy Booking
        String bookingId = request.getBookingBillId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        // Đổi tiền VND -> USD
        BigDecimal usdTotal = BigDecimal.valueOf(booking.getGrandTotal())
                .divide(BigDecimal.valueOf(VND_TO_USD_RATE), 2, RoundingMode.HALF_UP);

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
        // Vẫn nối bookingId vào để backup, dù đã xử lý xong
        redirectUrls.setReturnUrl(payPalConfig.getSuccessUrl() + "?bookingId=" + bookingId);
        payment.setRedirectUrls(redirectUrls);

        // Tạo payment trên PayPal
        Payment createPayment = payment.create(apiContext);

        // =========================================================================
        // LOGIC AUTO SUCCESS (ĐỒNG BỘ VỚI ZALOPAY)
        // =========================================================================

        // 2. Gọi BookingService để chốt đơn ngay lập tức (Status -> PAID, Lưu Bill)
        // Lưu ý: Dùng createPayment.getId() làm transactionId cho khớp với PayPal thật
        SuccessfulPaymentResponse result = bookingService.processSuccessfulPayment(
                bookingId,
                createPayment.getId(),
                booking.getGrandTotal(),
                PaymentMethod.PAYPAL
        );

        // 3. Gửi email xác nhận ngay lập tức
        sendBookingConfirmationEmail(result.getBooking(), result.getStatus());

        log.info("AUTO-SUCCESS: PayPal Link Created & DB Updated for Booking {}", bookingId);

        // =========================================================================

        return createPayment.getLinks().stream()
                .filter(links -> "approval_url".equals(links.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_CREATE_FAILED));
    }

    // Hàm execute này giờ chỉ để làm cảnh hoặc verify lại (vì đơn đã chốt ở trên rồi)
    public Payment executeOrder(String paymentId, String payerId, String bookingId) {
        // Logic thực thi thật sự có thể bỏ qua hoặc chỉ log info
        // Nếu frontend gọi lại API này sau khi redirect, ta chỉ cần trả về OK
        log.info("User completed PayPal flow for Booking {}. (Already processed in createOrder)", bookingId);

        // Nếu muốn chuẩn chỉnh, có thể gọi API PayPal execute để hoàn tất transaction bên phía PayPal
        // để tiền thực sự trừ vào tài khoản Sandbox (nhưng không update DB của mình nữa)
        try {
            Payment payment = new Payment();
            payment.setId(paymentId);
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            log.warn("Lỗi execute PayPal (nhưng DB đã update rồi nên kệ): {}", e.getMessage());
            return null;
        }
    }

    private void sendBookingConfirmationEmail(Booking booking, PaymentStatus status) {
        if(status == PaymentStatus.COMPLETED) {
            try {
                emailService.sendBookingConfirmationEmail(booking);
                log.info("Email send successfully to {}", booking.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send email: {}", e.getMessage());
            }
        }
    }
}