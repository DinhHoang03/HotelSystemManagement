package com.humg.HotelSystemManagement.service.SecurityService;

import com.humg.HotelSystemManagement.configuration.security.SecurityConfig;
import com.humg.HotelSystemManagement.dto.UserPrincipal;
import com.humg.HotelSystemManagement.dto.request.security.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.LogOutRequest;
import com.humg.HotelSystemManagement.dto.request.security.jwt.RefreshRequest;
import com.humg.HotelSystemManagement.dto.response.security.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.security.jwt.IntrospectResponse;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import com.humg.HotelSystemManagement.service.RedisService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    // Config bảo mật chứa bcrypt encoder để mã hóa/matching mật khẩu.
    SecurityConfig securityConfig;
    RedisService redisService;
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

    //Phương thức thiết lập cookie
    private void setCookies(HttpServletResponse response, String token, String role) {
        //Cookie cho token
        Cookie tokenCookie = new Cookie("token", token);
        tokenCookie.setHttpOnly(true); //Ngăn JavaScript truy cập cookie
        tokenCookie.setSecure(true); //Chỉ gửi qua https
        tokenCookie.setPath("/"); //Có thể truy cập từ mọi đường dẫn
        tokenCookie.setMaxAge((int) VALID_DURATION * 3600); //Thời gian sống của cookie (Tính bằng giây)
        response.addCookie(tokenCookie);

        //Cookie cho role
        Cookie roleCookie = new Cookie("role", role);
        roleCookie.setPath("/");
        roleCookie.setMaxAge((int) VALID_DURATION * 3600);
        response.addCookie(roleCookie);
    }

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

    public void logout(LogOutRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        try{
            var signedToken = verifyToken(request.getToken(), true);

            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date expiredTime = signedToken.getJWTClaimsSet().getExpirationTime();

            /**@Param:expiredTime: thời điểm token hết hạn
             * @Param: System.currentTimeMillis(): Lấy time hiện tại
             * cả hai đều tính bằng miliseccond, công thức tính là expiredTime - time hiện tại = số thời gian còn lại để sống của token
             * chia cho 1000 để đổi từ mili giây sang giây
             */
            long remainingSeconds = (expiredTime.getTime() - System.currentTimeMillis()) /1000;
            //Redis lưu key blacklist:jwt trong đúng số giây còn sống đó. Sau khi token hết hạn thì key trong redis tự bay màu
            redisService.blacklistToken(jwtId, remainingSeconds);

            Cookie tokenCookie = new Cookie("token", null);
            tokenCookie.setMaxAge(0);
            tokenCookie.setPath("/");
            response.addCookie(tokenCookie);

            Cookie roleCookie = new Cookie("role", null);
            roleCookie.setMaxAge(0);
            roleCookie.setPath("/");
            response.addCookie(roleCookie);

        }catch (AppException e){
            log.info("Token is not valid!");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken(), true);
        var jwtId = signedToken.getJWTClaimsSet().getJWTID();
        var expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();

        long remainingSeconds = (expirationTime.getTime() - System.currentTimeMillis()) /1000;
        redisService.blacklistToken(jwtId, remainingSeconds);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var userPrincipal = findUser(username);

        var token = generateToken(userPrincipal);
        var role = buildScope(userPrincipal);

        setCookies(response, token, role);

        return AuthenticationResponse.builder()
                .token(token)
                .role(role)
                .authenticated(true)
                .build();
    }

    private UserPrincipal findUser(String username) {
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

            if(employee.get().getUserStatus().equals(UserStatus.PENDING) ||
                    employee.get().getUserStatus().equals(UserStatus.REJECTED))
                throw new AppException(AppErrorCode.USER_NOT_APPROVE);

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
        if(!verified || expriredTime.before(new Date())){
            throw new AppException(AppErrorCode.UNAUTHENTICATED);
        }

        //Nếu Token đã log out thì sẽ bị disable vào hệ thống
        if(redisService.isTokenBlacklisted(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(AppErrorCode.UNAUTHENTICATED);


        return signedJWT;
    }



    // Hàm tạo token JWT với username và role, ký bằng HS512.
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        String username = request.getUsername();
        String password = request.getPassword();

        var user = findUser(username);
        var passwordToCheck = user.getPassword();

        // So khớp mật khẩu
        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(password, passwordToCheck);

        log.info("Password match: {}", authenticated);
        // Nếu mật khẩu không khớp, ném ngoại lệ UNAUTHENTICATED
        if (!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        // Tạo token JWT với đối tượng user đã có
        var token = generateToken(userPrincipal);
        var role = buildScope(userPrincipal);

        setCookies(response, token, role);

        // Trả về response chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .role(role)
                .authenticated(true)
                .build();
    }

    // Thay đổi generateToken để chấp nhận đối tượng
    private String generateToken(UserPrincipal user) {
        // Header và các thông tin chung như cũ
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Tạo payload với các thông tin như subject, issuer, thời gian tạo và hết hạn.
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

    public String getRole(String token) throws ParseException, JOSEException {
           var signedToken = verifyToken(token, false);
           var scope = signedToken.getJWTClaimsSet().getClaim("scope");
           if (scope instanceof String) {
               return (String) scope;
           }else {
               throw new AppException(AppErrorCode.NO_ROLES_ASSIGNED);
           }
    }
}