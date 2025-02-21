package com.humg.HotelSystemManagement.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
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
    String identityId;

    String name;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;

    @Email(message = "INVALID_EMAIL")
    String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
            message = "INVALID_PASSWORD")
    String password;
}
