package com.humg.HotelSystemManagement.dto;

import com.humg.HotelSystemManagement.entity.authorizezation.Role;
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
