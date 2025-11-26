package com.humg.HotelSystemManagement.modules.auth_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.*;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.AuthenticationResponse;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.IntrospectResponse;
import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.customer_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.email_service.resources.requests.NewPasswordRequest;
import com.humg.HotelSystemManagement.modules.redis_service.services.ExTokenHandleService;
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

    UserRepository userRepository; // Chỉ dùng 1 Repo duy nhất
    ExTokenHandleService exTokenHandleService;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // --- XỬ LÝ ĐĂNG NHẬP & TẠO TOKEN ---
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Tìm User trong bảng chung
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // 2. Check Pass
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        // 3. Check Status (Enabled mới cho vào)
        if (user.getUserStatus() != UserStatus.ENABLED) {
            throw new AppException(AppErrorCode.USER_NOT_APPROVE);
        }

        // 4. Tạo Token
        var tokenPair = generateTokenPair(user);

        return AuthenticationResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .authenticated(true)
                .roles(user.getRoles())
                .build();
    }

    private String generateToken(User user, String tokenType, long durationTime) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Khởi tạo Builder
        JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(durationTime, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("token-type", tokenType); // Lưu loại token (access/refresh)

        // CHỈ THÊM SCOPE NẾU LÀ ACCESS TOKEN
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
                // Đảm bảo role có prefix ROLE_
                String roleName = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
                stringJoiner.add(roleName);

                if (!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(p -> stringJoiner.add(p.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }

    // --- CÁC HÀM KHÁC (REFRESH, INTROSPECT...) ---

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    // --- SỬA HÀM LOGOUT ---
    // Nhận trực tiếp String token thay vì LogOutRequest
    public void logout(String token) throws ParseException, JOSEException {
        if (token == null || token.isEmpty()) {
            log.info("Token is null or empty, skip logout logic");
            return;
        }

        try {
            // Verify token để lấy thông tin (JTI, Expiration)
            // Lưu ý: verifyToken có thể ném lỗi nếu token hết hạn, ta cần bắt lỗi để không chặn luồng logout
            var signedToken = verifyToken(token);

            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();

            // Tính thời gian còn lại để lưu vào Redis Blacklist
            long remainingSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;

            if (remainingSeconds > 0) {
                exTokenHandleService.blackListToken(jwtId, remainingSeconds);
            }
        } catch (AppException e) {
            log.info("Token already invalid or expired, no need to blacklist.");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken());
        var jwtId = signedToken.getJWTClaimsSet().getJWTID();
        var expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();

        // Blacklist token cũ
        long remainingSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
        exTokenHandleService.blackListToken(jwtId, remainingSeconds);

        // Tạo token mới
        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        var tokenPair = generateTokenPair(user);

        return AuthenticationResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .authenticated(true)
                .roles(user.getRoles())
                .build();
    }

    // --- ĐỔI MẬT KHẨU ---
    public void changePassword(NewPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // Check username khớp email (bảo mật thêm)
        if (!user.getUsername().equals(request.getUsername())) {
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // --- VERIFY TOKEN ---
    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

//        Date expirationTime = (isRefresh)
//                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.DAYS).toEpochMilli())
//                : signedJWT.getJWTClaimsSet().getExpirationTime();

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!verified || expirationTime.before(new Date())) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        if (exTokenHandleService.isTokenBlackListed(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(AppErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private TokenPair generateTokenPair(User user) {
        String accessToken = generateToken(user, "access", VALID_DURATION);
        String refreshToken = generateToken(user, "refresh", REFRESHABLE_DURATION);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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