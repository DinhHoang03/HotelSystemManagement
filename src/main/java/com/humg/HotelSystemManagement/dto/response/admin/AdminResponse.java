package com.humg.HotelSystemManagement.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminResponse {
    Long adminId;

    String name;

    String email;

    String phone;
}
