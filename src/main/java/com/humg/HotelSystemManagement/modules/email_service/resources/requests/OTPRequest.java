package com.humg.HotelSystemManagement.modules.email_service.resources.requests;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OTPRequest {
    @NotNull(message = "REQUEST_NULL")
    String email;
    @NotNull(message = "REQUEST_NULL")
    String otp;
}
