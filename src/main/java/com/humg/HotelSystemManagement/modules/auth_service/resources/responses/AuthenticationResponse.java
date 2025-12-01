package com.humg.HotelSystemManagement.modules.auth_service.resources.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    String accessToken;

    @JsonIgnore
    String refreshToken;

    boolean authenticated;
    Set<Role> roles;
}
