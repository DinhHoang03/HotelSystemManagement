package com.humg.HotelSystemManagement.dto.response.user.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeResponse {
    String id;

    String username;

    String name;

    String gender;

    LocalDate dob;

    String email;

    String phone;

    String address;

    String identityId;

    String userStatus;

    String role;
}
