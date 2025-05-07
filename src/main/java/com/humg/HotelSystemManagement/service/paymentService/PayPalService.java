package com.humg.HotelSystemManagement.service.paymentService;

import com.humg.HotelSystemManagement.configuration.payment.PayPalConfig;
import com.humg.HotelSystemManagement.dto.request.payment.PayPalOrderRequest;
import com.humg.HotelSystemManagement.dto.request.payment.ZaloPayOrderRequest;
import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.booking.BookingBill;
import com.humg.HotelSystemManagement.entity.booking.PaymentBill;
import com.humg.HotelSystemManagement.entity.enums.PaymentMethod;
import com.humg.HotelSystemManagement.entity.enums.PaymentStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import com.humg.HotelSystemManagement.repository.booking.PaymentBillRepository;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingService;
import com.humg.HotelSystemManagement.service.HotelService.email.EmailService;
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
import java.time.LocalDate;
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
    PaymentBillRepository paymentBillRepository;
    BookingBillRepository bookingBillRepository;
    EmailService emailService;
    BookingService bookingService;

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
        transaction.setDescription("DinhRiseHotel - Payment for the order #" + request.getBookingBillId());
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

        //Tạo đối tượng Payment với ID từ paypal
        Payment payment = new Payment();
        payment.setId(paymentId);

        //Tạo đối tượng thực thi để thanh toán
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        //Gọi API PayPal để thực thi thanh toán
        Payment executedPayment =  payment.execute(apiContext, paymentExecution);

        //Lấy thông tin giao dịch
        Transaction transaction = executedPayment.getTransactions().get(0);
        String description = transaction.getDescription();

        //Lấy bookingBillId
        String bookingBillId = description.replace("DinhRiseHotel - Payment for the order #", "");
        if (bookingBillId.isEmpty()) throw new AppException(AppErrorCode.STRING_NULL);

        //Lấy thông tin BookingBill và Customer
        var bookingBill = bookingBillRepository.findById(bookingBillId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        var customer = bookingBill.getBooking().getCustomer();

        //Lấy thông tin giao dịch
        RelatedResources resources = transaction.getRelatedResources().get(0);
        Sale sale = resources.getSale();

        String transactionId = sale.getId();
        String currency = sale.getAmount().getCurrency();
        String totalAmount = sale.getAmount().getTotal();

        //Chuyển đổi tiền tệ từ $ sang VND
        BigDecimal dollarToVND = new BigDecimal(26000);
        BigDecimal totalAmountVND = new BigDecimal(totalAmount)
                .multiply(dollarToVND)
                .setScale(0, RoundingMode.HALF_UP);
        Long paidAmount = totalAmountVND.longValue();

        //Lưu payment xuống database
        PaymentBill paymentBill = PaymentBill.builder()
                .transactionId(transactionId)
                .paymentMethod(PaymentMethod.PAYPAL)
                .paidAmount(paidAmount)
                .status(PaymentStatus.COMPLETED)
                .createAt(LocalDate.now())
                .customer(customer)
                .build();

        var result = paymentBillRepository.save(paymentBill);

        var bookingId = bookingBill.getBooking().getBookingId();
        var paymentResultId = result.getPaymentId();
        bookingService.updatePaymentStatus(bookingId, paymentResultId);

        var booking = bookingBill.getBooking();
        var status = result.getStatus();
        sendBookingConfirmationEmail(booking, status);

        return payment.execute(apiContext, paymentExecution);
    }

    private void sendBookingConfirmationEmail(Booking booking, PaymentStatus status) {
        if(status == PaymentStatus.COMPLETED)
            emailService.sendBookingConfirmationEmail(booking);
    }

}