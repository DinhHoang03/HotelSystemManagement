package com.humg.HotelSystemManagement.configs;

import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import com.humg.HotelSystemManagement.modules.employee_service.models.entities.Employee;
import com.humg.HotelSystemManagement.modules.employee_service.models.repositories.EmployeeRepository;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(EmployeeRepository employeeRepository, RoleRepository roleRepository){
        return args -> {
            var roles = roleRepository.findById("ADMIN")
                    .orElseGet(
                            () -> {
                                Role newRole = Role.builder()
                                        .name("ADMIN")
                                        .build();

                                return roleRepository.save(newRole);
                            }
                    );

            if(employeeRepository.findByUsername("admin").isEmpty()){
                Employee admin = Employee.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(Set.of(roles))
                        .userStatus(UserStatus.APPROVED)
                        .build();

                employeeRepository.save(admin);
                log.warn("Admin user has been created with default password: admin, please change it");
            }
        };
    }
}
