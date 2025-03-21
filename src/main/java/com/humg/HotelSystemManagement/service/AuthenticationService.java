package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.UserPrincipal;
import com.humg.HotelSystemManagement.dto.request.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.LogOutRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.RefreshRequest;
import com.humg.HotelSystemManagement.dto.response.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.jwt.IntrospectResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.InvalidatedToken;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.authenticationRepository.InvalidatedTokenRepository;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

// Đánh dấu lớp này là một Spring Service, sẽ được quản lý bởi Spring IoC container.
@Slf4j
@Service
// Sử dụng Lombok để tự động tạo constructor với các dependency (final fields).
@RequiredArgsConstructor
// Đặt các field ở mức truy cập PRIVATE và mặc định là final (trừ khi có @NonFinal).
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    // Các repository để truy vấn thông tin Customer và Employee từ database.
    EmployeeRepository employeeRepository;
    CustomerRepository customerRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    // Config bảo mật chứa bcrypt encoder để mã hóa/matching mật khẩu.
    SecurityConfig securityConfig;
    @NonFinal
    UserPrincipal userPrincipal;

    // Khóa bí mật để ký JWT, lấy từ file cấu hình (application.properties/yaml).
    @NonFinal // Cho phép thay đổi giá trị trong runtime (từ @Value).
    @Value("${jwt.signerKey}") // Inject giá trị của "jwt.signerKey" từ cấu hình.
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // Hàm kiểm tra tính hợp lệ của token JWT (introspection).
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        //Kiểm tra hợp lệ nếu đăng nhập vào hệ thống
        try {
            verifyToken(token, false);
        }catch (AppException e){
            isValid = false;
        }

        // Trả về response với trạng thái hợp lệ của token.
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public void logout(LogOutRequest request) throws ParseException, JOSEException {
        try{
            var signedToken = verifyToken(request.getToken(), true);

            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date expriredTime = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken
                    .builder()
                    .id(jwtId)
                    .expriredTime(expriredTime.toString())
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        }catch (AppException e){
            log.info("Token is not valid!");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken(), true);
        var jwtId = signedToken.getJWTClaimsSet().getJWTID();
        var expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jwtId)
                .expriredTime(expirationTime.toString())
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var userPrincipal = findUser(username);

        var token = generateToken(userPrincipal);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public UserPrincipal findUser(String username) {
        var customer = customerRepository.findByUsername(username);
        var employee = employeeRepository.findByUsername(username);

        if (customer.isEmpty() && employee.isEmpty()) {
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        if(customer.isPresent()){
            var user = customer.get();
            var role = customer.get().getRoles();
            var password = customer.get().getPassword();

            userPrincipal = new UserPrincipal(
                    username,
                    password,
                    role.stream().toList()
            );
        } else if (employee.isPresent()) {
            var user = employee.get();
            var role = employee.get().getRoles();
            var password = employee.get().getPassword();

            userPrincipal = new UserPrincipal(
                    username,
                    password,
                    role.stream().toList()
            );
        }

        return userPrincipal;
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // Tạo verifier để kiểm tra chữ ký của token bằng khóa SIGNER_KEY.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        // Parse token thành đối tượng SignedJWT để truy cập các thành phần (header, payload, signature).
        SignedJWT signedJWT = SignedJWT.parse(token);
        // Lấy thời gian hết hạn từ claims trong token.
        Date expriredTime = (isRefresh) ?
                new Date(
                        signedJWT
                                .getJWTClaimsSet()
                                .getIssueTime()
                                .toInstant()
                                .plus(REFRESHABLE_DURATION, ChronoUnit.DAYS)
                                .toEpochMilli()
                )
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        // Kiểm tra chữ ký của token có hợp lệ không (dùng SIGNER_KEY và HS512).
        var verified = signedJWT.verify(verifier);

        // Token hợp lệ nếu chữ ký đúng và chưa hết hạn.
        if(!verified && expriredTime
                .after(
                        new Date()
                )
        ){
            throw new AppException(
                    AppErrorCode.UNAUTHENTICATED
            );
        }

        //Nếu Token đã log out thì sẽ bị disable vào hệ thống
        if(invalidatedTokenRepository.existsById(
                signedJWT
                        .getJWTClaimsSet()
                        .getJWTID())
        ){
            throw new AppException(
                    AppErrorCode.UNAUTHENTICATED
            );
        }

        return signedJWT;
    }



    // Hàm tạo token JWT với username và role, ký bằng HS512.
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        /**
        // Tìm user trong cả hai bảng Customer và Employee.
        var customer = customerRepository.findByUsername(username);
        var employee = employeeRepository.findByUsername(username);

        // Nếu không tìm thấy user ở cả hai bảng, ném ngoại lệ USER_NOT_EXISTED.
        if (customer.isEmpty() && employee.isEmpty()) {
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        // Biến để lưu mật khẩu và vai trò của user (Customer hoặc Employee).
        String passwordToCheck;
        //Object user = null;  // Lưu đối tượng người dùng để truyền vào buildScope

        // Kiểm tra user là Customer hay Employee, lấy mật khẩu và role tương ứng.
        if (customer.isPresent()) {
            var user = customer.get();
            passwordToCheck = customer.get().getPassword();
            userPrincipal = new UserPrincipal(
                    username,
                    user.getRoles().stream().toList()
            );
        } else {
            var user = employee.get();
            passwordToCheck = employee.get().getPassword();
            userPrincipal = new UserPrincipal(
                    username,
                    user.getRoles().stream().toList()
            );
        }
        */
        var user = findUser(username);
        var passwordToCheck = user.getPassword();

        // So khớp mật khẩu
        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(password, passwordToCheck);

        log.info("Password match: {}", authenticated);
        // Nếu mật khẩu không khớp, ném ngoại lệ UNAUTHENTICATED.
        if (!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        // Tạo token JWT với đối tượng user đã có
        var token = generateToken(userPrincipal);

        // Trả về response chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // Thay đổi generateToken để chấp nhận đối tượng
    private String generateToken(UserPrincipal user) {
        // Header và các thông tin chung như cũ
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                    .plus(VALID_DURATION, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))  // Truyền đối tượng user vào buildScope
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(AppErrorCode.SIGN_TOKEN_ERROR);
        }
    }
    
    // Thay đổi buildScope để chấp nhận đối tượng user
    private String buildScope(UserPrincipal user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(
                    role -> {
                        stringJoiner.add("ROLE_" + role.getName());
                        if (!CollectionUtils.isEmpty(role.getPermissions())){
                            role.getPermissions().forEach(permission -> {
                                stringJoiner.add(permission.getName());
                            });
                        }
                    }
            );
        }else {
            throw new AppException(AppErrorCode.NO_ROLES_ASSIGNED);
        }
        return stringJoiner.toString();
    }
}