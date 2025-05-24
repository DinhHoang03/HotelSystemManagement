package com.hotel.humg.HotelSystemManagement.dto.response.user.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckOutResponse {
    String checkOutDate;
    String employeeName;
}
