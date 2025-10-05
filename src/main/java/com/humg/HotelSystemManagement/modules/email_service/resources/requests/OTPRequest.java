package com.humg.HotelSystemManagement.modules.email_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OTPRequest {
    String email;
    String otp;
}
