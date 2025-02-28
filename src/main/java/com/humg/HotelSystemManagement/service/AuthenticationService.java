package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.jwt.AuthenticationRequest;
import com.humg.HotelSystemManagement.dto.response.jwt.AuthenticationResponse;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.CustomerRepository;
import com.humg.HotelSystemManagement.repository.employees.EmployeeRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        var customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        var employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        boolean authenticated = securityConfig.bcryptPasswordEncoder()
                .matches(request.getPassword(), customer.getPassword());

        if(!authenticated) throw new AppException(AppErrorCode.UNAUTHENTICATED);

        var token = generateToken(request.getEmail());

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public String generateToken(String email){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer("hotel.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(1, ChronoUnit.HOURS).toEpochMilli()))
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
