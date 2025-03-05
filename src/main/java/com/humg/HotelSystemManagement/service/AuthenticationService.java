package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.response.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.jwt.IntrospectResponse;
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

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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

    // Hàm xác thực người dùng dựa trên username và password, trả về token nếu thành công.
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        String username = request.getUsername();
        String password = request.getPassword();

        // Tìm user trong cả hai bảng Customer và Employee.
        var customer = customerRepository.findByUsername(request.getUsername());
        var employee = employeeRepository.findByUsername(request.getUsername());

        // Nếu không tìm thấy user ở cả hai bảng, ném ngoại lệ USER_NOT_EXISTED.
        if(customer.isEmpty() && employee.isEmpty()){
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        // Biến để lưu mật khẩu và vai trò của user (Customer hoặc Employee).
        String passwordToCheck;
        String role;

        // Kiểm tra user là Customer hay Employee, lấy mật khẩu và role tương ứng.
        if(customer.isPresent()){
            passwordToCheck = customer.get().getPassword();
            role = customer.get().getRole().toString();
        }else{
            Employee emp = employee.get();
            passwordToCheck = emp.getPassword();
            role = emp.getRole().toString();
        }

        // So khớp mật khẩu người dùng nhập với mật khẩu đã mã hóa trong DB bằng bcrypt.
        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(password, passwordToCheck);

        // Nếu mật khẩu không khớp, ném ngoại lệ UNAUTHENTICATED.
        if(!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        // Tạo token JWT với username và role nếu xác thực thành công.
        var token = generateToken(username, role);

        // Trả về response chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

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
    private String generateToken(String username, String role){
        // Tạo header với thuật toán HS512.
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Tạo payload chứa các claim: username, issuer, thời gian phát hành/hết hạn, và role.
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username) // Username làm subject (principal).
                .issuer("hotel.com") // Issuer của token.
                .issueTime(new Date()) // Thời gian phát hành token.
                .expirationTime(new Date(Instant.now()
                        .plus(1, ChronoUnit.HOURS).toEpochMilli())) // Hết hạn sau 1 giờ.
                .claim("role", role) // Lưu vai trò/quyền vào claim "role".
                .build();

        // Chuyển claims thành payload dạng JSON.
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // Tạo JWSObject từ header và payload.
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Ký token bằng khóa SIGNER_KEY và thuật toán HS512.
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            // Trả về token dưới dạng chuỗi serialized (header.payload.signature).
            return jwsObject.serialize();
        } catch (JOSEException e) {
            // Nếu lỗi khi ký, ném ngoại lệ SIGN_TOKEN_ERROR.
            throw new AppException(AppErrorCode.SIGN_TOKEN_ERROR); //Check error code này
        }
    }
}