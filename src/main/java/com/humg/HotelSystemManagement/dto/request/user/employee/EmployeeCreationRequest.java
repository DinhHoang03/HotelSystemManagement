package com.humg.HotelSystemManagement.dto.request.user.employee;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeCreationRequest {
    @NotNull(message = "REQUEST_NULL")
    String name;

    @Size(min = 4, message = "INVALID_USERNAME")
    @NotNull(message = "REQUEST_NULL")
    String username;

    @Email(message = "INVALID_EMAIL")
    @NotNull(message = "REQUEST_NULL")
    String email;

    @NotNull(message = "REQUEST_NULL")
    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;

    @Size(max = 12, message = "INVALID_IDENTITY_ID")
    @NotNull(message = "REQUEST_NULL")
    String identityId;

    @NotNull(message = "REQUEST_NULL")
    @Past(message = "INVALID_DOB")
    LocalDate dob;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "INVALID_GENDER")
    String gender;

    @NotNull(message = "REQUEST_NULL")
    String address;

    @Pattern(regexp = "^(ACCOUNTANT|DEPARTMENT_HEAD|RECEPTIONIST|CLEANER|WAITER)$",
            message = "INVALID_ROLE")
    @NotNull(message = "REQUEST_NULL")
    String role;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$", message = "INVALID_PASSWORD")
    @NotNull(message = "REQUEST_NULL")
    String password;
}
