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
    EmailService emailService;

    // Define tỷ giá (Tốt nhất nên lấy từ DB cấu hình hoặc API tỷ giá thực)
    private static final double VND_TO_USD_RATE = 26000.0;

    /**
     * BƯỚC 1: TẠO PAYMENT LINK ĐỂ USER ĐI THANH TOÁN
     */
    public String createOrder(PayPalOrderRequest request) throws PayPalRESTException {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        // Lấy Booking
        String bookingId = request.getBookingId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        // Đổi tiền VND -> USD
        BigDecimal usdTotal = BigDecimal.valueOf(booking.getGrandTotal())
                .divide(BigDecimal.valueOf(VND_TO_USD_RATE), 2, RoundingMode.HALF_UP);

        // Cấu hình số tiền
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(usdTotal.toPlainString());

        // Mô tả giao dịch
        Transaction transaction = new Transaction();
        transaction.setDescription("Payment for Booking #" + booking.getBookingId());
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Phương thức thanh toán
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Tạo đối tượng Payment
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Cấu hình Redirect URL (Nơi User quay về sau khi thao tác trên PayPal)
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(payPalConfig.getCancelUrl());

        // Gắn bookingId vào returnUrl để Frontend biết đơn nào vừa thanh toán xong
        redirectUrls.setReturnUrl(payPalConfig.getSuccessUrl() + "?bookingId=" + bookingId);

        payment.setRedirectUrls(redirectUrls);

        // Gửi request tạo Payment sang PayPal
        Payment createPayment = payment.create(apiContext);

        // Trả về link 'approval_url' cho Frontend redirect
        return createPayment.getLinks().stream()
                .filter(links -> "approval_url".equals(links.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_CREATE_FAILED));
    }

    /**
     * BƯỚC 2: XỬ LÝ KHI USER QUAY LẠI (EXECUTE & UPDATE DB)
     * Hàm này được gọi từ Frontend sau khi PayPal redirect về
     */
    public SuccessfulPaymentResponse executeOrder(String paymentId, String payerId, String bookingId) {
        // 1. Kiểm tra Booking trong DB trước (QUAN TRỌNG: Chống trùng lặp)
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        // ==> FIX QUAN TRỌNG: Nếu đơn đã thanh toán rồi thì trả về Success luôn
        // Tránh lỗi Frontend gọi 2 lần hoặc mạng lag
        if (booking.getPaymentStatus() == PaymentStatus.COMPLETED
                || booking.getPaymentStatus() == PaymentStatus.PAID) {

            log.info("Booking {} was already paid. Returning success immediately to Frontend.", bookingId);

            return SuccessfulPaymentResponse.builder()
                    .booking(booking)
                    .status(PaymentStatus.COMPLETED)
                    .build();
        }

        try {
            // 2. Cấu hình execute request gửi lên PayPal để trừ tiền
            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            // 3. Gọi API execute
            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            // 4. Kiểm tra trạng thái trả về
            if (executedPayment.getState().equals("approved")) {
                log.info("PayPal Payment Approved. TransactionID: {}", paymentId);

                // 5. UPDATE DB: Chuyển trạng thái sang PAID và tạo Payment Bill
                SuccessfulPaymentResponse result = bookingService.processSuccessfulPayment(
                        bookingId,
                        paymentId, // Dùng paymentId làm mã giao dịch
                        booking.getGrandTotal(),
                        PaymentMethod.PAYPAL
                );

                // 6. Gửi email xác nhận (Bọc try-catch để an toàn)
                // Nếu gửi mail lỗi thì vẫn coi là thanh toán thành công
                try {
                    sendBookingConfirmationEmail(result.getBooking(), result.getStatus());
                } catch (Exception ex) {
                    log.warn("Payment success but failed to send email for Booking {}: {}", bookingId, ex.getMessage());
                }

                return result;
            } else {
                log.error("PayPal Payment not approved. State: {}", executedPayment.getState());
                throw new AppException(AppErrorCode.PAYMENT_FAILED);
            }

        } catch (PayPalRESTException e) {
            // Check nếu lỗi là do "Payment has already been done" (Trường hợp hiếm)
            if (e.getMessage() != null &&
                    (e.getMessage().contains("PAYMENT_ALREADY_DONE") || e.getMessage().contains("capture"))) {

                log.info("PayPal reported payment already done provided for Booking {}", bookingId);

                // Force update DB nếu cần thiết hoặc trả về success
                return SuccessfulPaymentResponse.builder()
                        .booking(booking)
                        .status(PaymentStatus.COMPLETED)
                        .build();
            }

            log.error("PayPal Execute Error: {}", e.getMessage());
            throw new AppException(AppErrorCode.PAYMENT_FAILED);
        }
    }

    private void sendBookingConfirmationEmail(Booking booking, PaymentStatus status) {
        if(status == PaymentStatus.COMPLETED) {
            try {
                emailService.sendBookingConfirmationEmail(booking);
                log.info("Email sent successfully to {}", booking.getUser().getEmail());
            } catch (Exception e) {
                // Chỉ ném lỗi để hàm gọi bên ngoài (executeOrder) bắt và log warning
                throw new RuntimeException(e);
            }
        }
    }
}