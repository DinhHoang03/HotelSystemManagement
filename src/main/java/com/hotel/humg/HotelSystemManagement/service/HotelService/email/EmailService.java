package com.hotel.humg.HotelSystemManagement.service.HotelService.email;

import com.hotel.humg.HotelSystemManagement.dto.request.email.EmailRequest;
import com.hotel.humg.HotelSystemManagement.entity.booking.Booking;
import com.hotel.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.hotel.humg.HotelSystemManagement.exception.exceptions.AppException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender mailSender;
    SpringTemplateEngine templateEngine;

    @NonFinal
    @Value("${spring.mail.username}")
    protected String fromEmail;

    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(booking.getCustomer().getEmail());
            helper.setSubject("Xác nhận đặt phòng thành công - DinhRiseHotel");
            helper.setFrom(fromEmail);

            // Chuẩn bị dữ liệu cho Thymeleaf
            Context context = new Context();
            context.setVariable("customerName", booking.getCustomer().getName());
            context.setVariable("bookingId", booking.getBookingId());
            context.setVariable("bookingDate", booking.getBookingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("grandTotal", booking.getGrandTotal());
            context.setVariable("bookingRooms", booking.getBookingRooms()); // Truyền danh sách phòng trực tiếp

            // Render nội dung từ template
            String htmlContent = templateEngine.process("booking-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email xác nhận đặt phòng đã được gửi đến: {}", booking.getCustomer().getEmail());

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email xác nhận đặt phòng cho {}: {}", booking.getCustomer().getEmail(), e.getMessage());
            throw new RuntimeException("Không thể gửi email xác nhận đặt phòng", e);
        }
    }

    public void sendOTPEmail(EmailRequest emailRequest, String otp) {
        if (emailRequest == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        var toEmail = emailRequest.getEmail();

        try {
            log.info("Preparing to send OTP email to: {}", toEmail);
            log.info("OTP value being sent: {}", otp);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP đặt lại mật khẩu - DinhRise Hotel");
            helper.setFrom(fromEmail);

            // Tạo context với Locale để đảm bảo encoding đúng
            Context context = new Context(Locale.forLanguageTag("vi-VN"));
            context.setVariable("otp", otp);

            log.info("Context variables: otp = {}", context.getVariable("otp"));

            String htmlContent = templateEngine.process("otp-email", context);
            log.info("Generated HTML content contains OTP: {}", htmlContent.contains(otp));

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email OTP đã được gửi đến: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email OTP cho {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi mail OTP cho user", e);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý template email OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xử lý template email", e);
        }
    }
}