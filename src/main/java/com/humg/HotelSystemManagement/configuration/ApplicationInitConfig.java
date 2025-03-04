package com.humg.HotelSystemManagement.configuration;

import com.humg.HotelSystemManagement.repository.employees.EmployeeRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Configuration
public class ApplicationInitConfig {
    ApplicationRunner applicationRunner(EmployeeRepository employeeRepository){
        return args -> {
            if(employeeRepository.findByEmail("admin").isEmpty()){
                var roles = new HashSet<String>();
                roles.add("ADMIN")
            }
        }
    }
}
