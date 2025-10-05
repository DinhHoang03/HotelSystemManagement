package com.humg.HotelSystemManagement.modules.redis_service.services;

import com.humg.HotelSystemManagement.modules.email_service.resources.requests.EmailRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.OTPRequest;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.services.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OTPService {
    AuthenticationService authenticationService;

    RedisTemplate<String, String> redisTemplateString;
    static Random random = new SecureRandom();

    public String generateOTP(EmailRequest request) {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var email = request.getEmail();

        String otp = String.format("%06d", random.nextInt(1_000_000));
        saveOTP(email, otp);
        return otp;
    }

    public void saveOTP(String email, String otp) {
        String key = "otp:" + email;
        redisTemplateString.opsForValue().set(key, otp, 5, TimeUnit.MINUTES); // TTL 5 phút
    }

    public boolean verifyOTP(OTPRequest request) {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        String email = request.getEmail();
        String otp = request.getOtp();

        String key = "otp:" + email;
        String savedOtp = redisTemplateString.opsForValue().get(key);
        return savedOtp != null && savedOtp.equals(otp);
    }

    public void deleteOTP(EmailRequest emailRequest) {
        if (emailRequest == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        var email = emailRequest.getEmail();

        redisTemplateString.delete("otp:" + email);
    }
}
