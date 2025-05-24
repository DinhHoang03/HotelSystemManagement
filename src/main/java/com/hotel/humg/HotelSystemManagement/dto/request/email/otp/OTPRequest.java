package com.hotel.humg.HotelSystemManagement.dto.request.email.otp;

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
