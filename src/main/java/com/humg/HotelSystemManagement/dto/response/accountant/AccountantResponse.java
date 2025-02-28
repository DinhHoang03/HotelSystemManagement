package com.humg.HotelSystemManagement.dto.response.accountant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountantResponse {
    Long id;

    String name;

    String email;

    String phone;

    String role;
}
