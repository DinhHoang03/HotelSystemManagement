package com.humg.HotelSystemManagement.modules.employee_service.resources.responses;

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
