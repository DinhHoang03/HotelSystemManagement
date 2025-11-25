package com.humg.HotelSystemManagement.modules.auth_service.resources.responses;

import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    boolean authenticated;
    Set<Role> roles;
}
