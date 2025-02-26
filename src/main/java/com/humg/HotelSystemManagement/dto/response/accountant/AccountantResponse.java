package com.humg.HotelSystemManagement.dto.response.accountant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountantResponse {
    Long accountantId;

    String name;

    String email;

    String phone;
}
