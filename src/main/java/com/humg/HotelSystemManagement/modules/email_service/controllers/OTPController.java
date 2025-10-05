package com.humg.HotelSystemManagement.modules.email_service.controllers;

import com.humg.HotelSystemManagement.modules.email_service.resources.requests.EmailRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.NewPasswordRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.OTPRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.email_service.services.EmailService;
import com.humg.HotelSystemManagement.modules.auth_service.services.AuthenticationService;
import com.humg.HotelSystemManagement.modules.redis_service.services.OTPService;
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
