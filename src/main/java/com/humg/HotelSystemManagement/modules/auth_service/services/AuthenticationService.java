package com.humg.HotelSystemManagement.modules.auth_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.*;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.AuthenticationResponse;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.IntrospectResponse;
import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.user_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.user_service.services.UserService;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.EmailRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.NewPasswordRequest;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.OTPRequest;
import com.humg.HotelSystemManagement.modules.email_service.services.EmailService;
import com.humg.HotelSystemManagement.modules.redis_service.services.ExTokenHandleService;
import com.humg.HotelSystemManagement.modules.redis_service.services.OTPService;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    ExTokenHandleService exTokenHandleService;
    PasswordEncoder passwordEncoder;
    OTPService otpService;
    EmailService emailService;
    UserService userService;
    RedisTemplate<String, Object> redisTemplateObject;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION_MINUTES;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_STANDARD_MINUTES;

    @NonFinal
    @Value("${jwt.remember-me-duration}")
    protected long REMEMBER_ME_MINUTES;

    // --- ĐĂNG NHẬP ---
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        if (user.getUserStatus() != UserStatus.ENABLED) {
            throw new AppException(AppErrorCode.USER_NOT_APPROVE);
        }

        // Truyền flag rememberMe để tính thời gian token
        var tokenPair = generateTokenPair(user, request.isRememberMe());

        return AuthenticationResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .authenticated(true)
                .roles(user.getRoles())
                .build();
    }

    // --- TẠO CẶP TOKEN ---
    private TokenPair generateTokenPair(User user, boolean isRememberMe) {
        String accessToken = generateToken(user, "access", VALID_DURATION_MINUTES);

        // Chọn thời gian dựa vào flag (1 ngày hoặc 14 ngày)
        long refreshDuration = isRememberMe ? REMEMBER_ME_MINUTES : REFRESH_STANDARD_MINUTES;
        String refreshToken = generateToken(user, "refresh", refreshDuration);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // --- LÀM MỚI TOKEN ---
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken());
        var jwtId = signedToken.getJWTClaimsSet().getJWTID();
        var expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();

        // Blacklist token cũ
        long remainingSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
        exTokenHandleService.blackListToken(jwtId, remainingSeconds);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // Khi refresh, mặc định reset về thời gian chuẩn (1 ngày) để an toàn
        var tokenPair = generateTokenPair(user, false);

        return AuthenticationResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .authenticated(true)
                .roles(user.getRoles())
                .build();
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC (GIỮ NGUYÊN) ---
    private String generateToken(User user, String tokenType, long durationMinutes) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(durationMinutes, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("token-type", tokenType);

        if ("access".equals(tokenType)) {
            claimBuilder.claim("scope", buildScope(user));
        }

        JWTClaimsSet jwtClaimsSet = claimBuilder.build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(AppErrorCode.SIGN_TOKEN_ERROR);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                String roleName = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
                stringJoiner.add(roleName);
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(p -> stringJoiner.add(p.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        if (!verified || expirationTime.before(new Date())) throw new AppException(AppErrorCode.UNAUTHENTICATED);
        if (exTokenHandleService.isTokenBlackListed(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(AppErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public void registerStep1(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }
        String key = "temp_reg:" + request.getEmail();
        redisTemplateObject.opsForValue().set(key, request, 10, java.util.concurrent.TimeUnit.MINUTES);
        EmailRequest emailReq = new EmailRequest();
        emailReq.setEmail(request.getEmail());
        String otp = otpService.generateOTP(emailReq);
        emailService.sendOTPEmail(emailReq, otp);
    }

    public AuthenticationResponse registerStep2_Verify(OTPRequest otpRequest) {
        String email = otpRequest.getEmail();
        if (!otpService.verifyOTP(otpRequest)) throw new AppException(AppErrorCode.UNAUTHENTICATED);
        String key = "temp_reg:" + email;
        UserCreationRequest registrationData = (UserCreationRequest) redisTemplateObject.opsForValue().get(key);
        if (registrationData == null) throw new AppException(AppErrorCode.SESSION_EXPIRED);
        userService.create(registrationData);
        redisTemplateObject.delete(key);
        otpService.deleteOTP(new EmailRequest(email));
        return authenticate(new AuthenticationRequest(registrationData.getUsername(), registrationData.getPassword(), false));
    }

    public void logout(String token) throws ParseException, JOSEException {
        if (token == null || token.isEmpty()) return;
        try {
            var signedToken = verifyToken(token);
            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
            long remainingSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
            if (remainingSeconds > 0) exTokenHandleService.blackListToken(jwtId, remainingSeconds);
        } catch (AppException e) { log.info("Token invalid, skip blacklist."); }
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try { verifyToken(token); } catch (AppException e) { isValid = false; }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    public void changePassword(NewPasswordRequest request) {
        OTPRequest otpCheck = new OTPRequest(request.getEmail(), request.getOtp());
        if (!otpService.verifyOTP(otpCheck)) throw new AppException(AppErrorCode.UNAUTHENTICATED);
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.deleteOTP(new EmailRequest(request.getEmail()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TokenPair {
        String accessToken;
        String refreshToken;
    }
}