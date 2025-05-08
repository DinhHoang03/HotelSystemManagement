package com.humg.HotelSystemManagement.dto.request.user.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreationRequest {
    @Size(max = 12, message = "INVALID_IDENTITY_ID")
    @NotNull(message = "REQUEST_NULL")
    String identityId;

    @Size(min = 4, message = "INVALID_USERNAME")
    @NotNull(message = "REQUEST_NULL")
    String username;

    @NotNull(message = "REQUEST_NULL")
    String name;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    @NotNull(message = "REQUEST_NULL")
    String phone;

    @Email(message = "INVALID_EMAIL")
    @NotNull(message = "REQUEST_NULL")
    String email;

    @NotNull(message = "REQUEST_NULL")
    @Past(message = "INVALID_DOB")
    LocalDate dob;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "INVALID_GENDER")
    String gender;

    @NotNull(message = "REQUEST_NULL")
    String address;

    @JsonIgnore
    String role;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
            message = "INVALID_PASSWORD")
    @NotNull(message = "REQUEST_NULL")
    String password;
}
