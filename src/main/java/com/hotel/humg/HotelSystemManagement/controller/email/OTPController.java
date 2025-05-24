package com.hotel.humg.HotelSystemManagement.controller.email;

import com.hotel.humg.HotelSystemManagement.dto.request.email.EmailRequest;
import com.hotel.humg.HotelSystemManagement.dto.request.email.otp.NewPasswordRequest;
import com.hotel.humg.HotelSystemManagement.dto.request.email.otp.OTPRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.APIResponse;
import com.hotel.humg.HotelSystemManagement.service.HotelService.email.EmailService;
import com.hotel.humg.HotelSystemManagement.service.SecurityService.AuthenticationService;
import com.hotel.humg.HotelSystemManagement.service.redis.OTPService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OTPController {
    OTPService otpService;
    EmailService emailService;
    AuthenticationService authenticationService;

    @PostMapping("/forgot-password/send-otp")
    APIResponse<?> sendOtp(@RequestBody EmailRequest emailRequest) {
        var otp = otpService.generateOTP(emailRequest);
        emailService.sendOTPEmail(emailRequest, otp);
        return APIResponse.builder()
                .message("Send OTP successfully")
                .build();
    }

    @PostMapping("/verify-otp")
    APIResponse<Boolean> verifyOtp(@RequestBody OTPRequest request) {
        var result = otpService.verifyOTP(request);
        return APIResponse.<Boolean>builder()
                .result(result)
                .message("Verify successfully!")
                .build();
    }

    @PostMapping("/delete-otp")
    APIResponse<?> deleteOtp(@RequestBody EmailRequest emailRequest) {
        otpService.deleteOTP(emailRequest);
        return APIResponse.builder()
                .message("Delete otp for email " + emailRequest.getEmail() + " successfully")
                .build();
    }

    @PostMapping("/forgot-password/update-password")
    APIResponse<?> changePassword(@RequestBody NewPasswordRequest request) {
        authenticationService.changePassword(request);
        return APIResponse.builder()
                .message("Update password successfully")
                .build();
    }
}
