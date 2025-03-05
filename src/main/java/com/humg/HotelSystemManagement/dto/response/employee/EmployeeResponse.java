package com.humg.HotelSystemManagement.dto.response.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    Long id;

    String username;

    String name;

    String gender;

    LocalDate dob;

    String email;

    String phone;

    String identityId;

    String userStatus;

    String role;
}
