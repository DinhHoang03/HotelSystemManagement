package com.humg.HotelSystemManagement.dto.request.waiter;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaiterCreationRequest {
    String name;

    String email;

    String phone;

    String password;
}
