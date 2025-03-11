package com.humg.HotelSystemManagement.configuration;

import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(EmployeeRepository employeeRepository){
        return args -> {
            if(employeeRepository.findByUsername("admin").isEmpty()){
                Employee admin = Employee.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        //.role(Roles.ADMIN)
                        .build();

                employeeRepository.save(admin);
                log.warn("Admin user has been created with default password: admin, please change it");
            }
        };
    }
}
