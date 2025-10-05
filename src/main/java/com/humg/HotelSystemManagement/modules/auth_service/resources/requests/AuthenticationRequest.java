package com.humg.HotelSystemManagement.modules.auth_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    //Chiều mở rộng thêm login có email hoặc phone
    String username;
    String password;
}
