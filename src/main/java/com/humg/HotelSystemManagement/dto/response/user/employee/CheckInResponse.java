package com.humg.HotelSystemManagement.dto.response.user.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckInResponse {
    String checkInDate;
    String employeeName;
}
