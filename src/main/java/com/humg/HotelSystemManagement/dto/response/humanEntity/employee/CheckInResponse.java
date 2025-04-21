package com.humg.HotelSystemManagement.dto.response.humanEntity.employee;

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
