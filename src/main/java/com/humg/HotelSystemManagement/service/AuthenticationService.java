package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.request.jwt.IntrospectRequest;
import com.humg.HotelSystemManagement.dto.response.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.dto.response.jwt.IntrospectResponse;
import com.humg.HotelSystemManagement.entity.employees.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.CustomerRepository;
import com.humg.HotelSystemManagement.repository.employees.EmployeeRepository;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    EmployeeRepository employeeRepository;
    CustomerRepository customerRepository;
    SecurityConfig securityConfig;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        String email = request.getEmail();
        String password = request.getPassword();

        var customer = customerRepository.findByEmail(request.getEmail());
        var employee = employeeRepository.findByEmail(request.getEmail());

        if(customer.isEmpty() || employee.isEmpty()){
            throw new AppException(AppErrorCode.USER_NOT_EXISTED);
        }

        String passwordToCheck;
        String role;

        if(customer.isPresent()){
            passwordToCheck = customer.get().getPassword();
            role = customer.get().getRole();
        }else{
            Employee emp = employee.get();
            passwordToCheck = emp.getPassword();
            role = emp.getRole();
        }

        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(password, passwordToCheck);

        if(!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        var token = generateToken(email, role);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expriredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        var result = verified && expriredTime.after(new Date());

        return IntrospectResponse.builder()
                .valid(result)
                .build();
    }

    private String generateToken(String email, String role){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("Role", role)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(AppErrorCode.SIGN_TOKEN_ERROR);
            //Tạm thời xử lý như này
        }
    }
}
