package com.humg.HotelSystemManagement.modules.auth_service.resources;

import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPrincipal {
    String username;
    String password;
    List<Role> roles;
}
