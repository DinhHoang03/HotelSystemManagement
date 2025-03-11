package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.response.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.jwt.IntrospectResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

// Đánh dấu lớp này là một Spring Service, sẽ được quản lý bởi Spring IoC container.
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

    // Khóa bí mật để ký JWT, lấy từ file cấu hình (application.properties/yaml).
    @NonFinal // Cho phép thay đổi giá trị trong runtime (từ @Value).
    @Value("${jwt.signerKey}") // Inject giá trị của "jwt.signerKey" từ cấu hình.
    protected String SIGNER_KEY;

    // Hàm kiểm tra tính hợp lệ của token JWT (introspection).
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        // Tạo verifier để kiểm tra chữ ký của token bằng khóa SIGNER_KEY.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // Parse token thành đối tượng SignedJWT để truy cập các thành phần (header, payload, signature).
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Lấy thời gian hết hạn từ claims trong token.
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Kiểm tra chữ ký của token có hợp lệ không (dùng SIGNER_KEY và HS512).
        var verified = signedJWT.verify(verifier);

        // Token hợp lệ nếu chữ ký đúng và chưa hết hạn.
        var result = verified && expiredTime.after(new Date());

        // Trả về response với trạng thái hợp lệ của token.
        return IntrospectResponse.builder()
                .valid(result)
                .build();
    }

    // Hàm tạo token JWT với username và role, ký bằng HS512.
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // Tìm user trong cả hai bảng Customer và Employee.
        var customer = customerRepository.findByUsername(username);
        var employee = employeeRepository.findByUsername(username);

        // Nếu không tìm thấy user ở cả hai bảng, ném ngoại lệ USER_NOT_EXISTED.
        if (customer.isEmpty() && employee.isEmpty()) {
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        // Biến để lưu mật khẩu và vai trò của user (Customer hoặc Employee).
        String passwordToCheck;
        Object user = null;  // Lưu đối tượng người dùng để truyền vào buildScope

        // Kiểm tra user là Customer hay Employee, lấy mật khẩu và role tương ứng.
        if (customer.isPresent()) {
            user = customer.get();
            passwordToCheck = customer.get().getPassword();
        } else {
            user = employee.get();
            passwordToCheck = employee.get().getPassword();
        }

        // So khớp mật khẩu
        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(password, passwordToCheck);

        // Nếu mật khẩu không khớp, ném ngoại lệ UNAUTHENTICATED.
        if (!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        // Tạo token JWT với đối tượng user đã có
        var token = generateToken(user);

        // Trả về response chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // Thay đổi generateToken để chấp nhận đối tượng
    private String generateToken(Object user) {
        // Header và các thông tin chung như cũ
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String username;
        if (user instanceof Customer) {
            username = ((Customer) user).getUsername();
        } else {
            username = ((Employee) user).getUsername();
        }

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("role", buildScope(user))  // Truyền đối tượng user vào buildScope
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
    private String buildScope(Object user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user instanceof Employee) {
            Employee emp = (Employee) user;
            if (!CollectionUtils.isEmpty(emp.getRoles())) {
                addRoleAndPermission(stringJoiner, emp.getRoles().stream().toList());
            }
        } else if (user instanceof Customer) {
            Customer cus = (Customer) user;
            if (!CollectionUtils.isEmpty(cus.getRoles())) {
                addRoleAndPermission(stringJoiner, cus.getRoles().stream().toList());
            }
        }

        // Bạn có thể quyết định loại bỏ ngoại lệ này hoặc giữ lại tùy trường hợp
        if(stringJoiner.length() == 0){
            // Có thể gán vai trò mặc định thay vì ném ngoại lệ
            stringJoiner.add("ROLE_DEFAULT");
            // Hoặc giữ nguyên ném ngoại lệ
            // throw new AppException(AppErrorCode.NO_ROLES_ASSIGNED);
        }

        return stringJoiner.toString();
    }
    //Hàm lấy role và permission
    private void addRoleAndPermission(StringJoiner stringJoiner, List<Role> roles) {
        roles.forEach(
                role -> {
                    if (role != null && !StringUtils.isEmpty(role.getRoleName())) {
                        stringJoiner.add("ROLE_" + role.getRoleName());

                        if (!CollectionUtils.isEmpty(role.getPermissions())) {
                            role.getPermissions().forEach(
                                    permission -> {
                                        if (permission != null && !StringUtils.isEmpty(permission.getPermissionName())) {
                                            stringJoiner.add(permission.getPermissionName());
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }
}