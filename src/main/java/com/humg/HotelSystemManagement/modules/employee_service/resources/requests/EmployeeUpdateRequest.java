package com.humg.HotelSystemManagement.modules.employee_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUpdateRequest {
    String password;
    String name;
    LocalDate dob;
    List<String> roles;
}
