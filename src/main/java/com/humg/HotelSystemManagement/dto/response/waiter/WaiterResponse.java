package com.humg.HotelSystemManagement.dto.response.waiter;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaiterResponse {
    Long waiterId;

    String name;

    String email;

    String phone;
}
