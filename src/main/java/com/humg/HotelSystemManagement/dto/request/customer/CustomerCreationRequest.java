package com.humg.HotelSystemManagement.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerCreationRequest {
    @Size(max = 12, message = "INVALID_IDENTITY_ID")
    @NotNull(message = "REQUEST_NULL")
    String identityId;

    @NotNull(message = "REQUEST_NULL")
    String name;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    @NotNull(message = "REQUEST_NULL")
    String phone;

    @Email(message = "INVALID_EMAIL")
    @NotNull(message = "REQUEST_NULL")
    String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
            message = "INVALID_PASSWORD")
    @NotNull(message = "REQUEST_NULL")
    String password;
}
