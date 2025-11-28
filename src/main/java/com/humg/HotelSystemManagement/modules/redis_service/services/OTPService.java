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
    RedisTemplate<String, String> redisTemplateString;
    static Random random = new SecureRandom();

    private static final long OTP_COOLDOWN_SECONDS = 60;

    public String generateOTP(EmailRequest request) {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        var email = request.getEmail();

        // 1. CHECK RATE LIMIT (KIỂM TRA HỒI CHIÊU)
        String spamKey = "otp_cooldown:" + email;

        // Kiểm tra xem key chặn có tồn tại không
        if (redisTemplateString.hasKey(spamKey)) {
            // Lấy thời gian còn lại (để báo lỗi chi tiết nếu muốn)
            Long expire = redisTemplateString.getExpire(spamKey, TimeUnit.SECONDS);
            throw new AppException(AppErrorCode.OTP_SPAM_DETECTED);
            // Hoặc throw lỗi kèm message: "Vui lòng chờ " + expire + " giây nữa."
        }

        // 2. LOGIC TẠO OTP CŨ
        String otp = String.format("%06d", random.nextInt(1_000_000));
        saveOTP(email, otp);

        // 3. KÍCH HOẠT RATE LIMIT (BẬT HỒI CHIÊU)
        // Lưu key chặn vào Redis với thời gian sống là 60s
        // Giá trị bên trong không quan trọng, để "1" là được
        redisTemplateString.opsForValue().set(spamKey, "1", OTP_COOLDOWN_SECONDS, TimeUnit.SECONDS);

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
