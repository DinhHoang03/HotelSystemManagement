package com.hotel.humg.HotelSystemManagement.dto.request.security.jwt;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogOutRequest {
    String token;
}