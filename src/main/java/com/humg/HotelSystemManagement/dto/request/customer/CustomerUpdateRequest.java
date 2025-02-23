package com.humg.HotelSystemManagement.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {
    @Email(message = "INVALID_EMAIL")
    @NotNull(message = "REQUEST_NULL")
    String email;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    @NotNull(message = "REQUEST_NULL")
    String phone;
}
