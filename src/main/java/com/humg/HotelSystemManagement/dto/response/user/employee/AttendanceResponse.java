package com.humg.HotelSystemManagement.dto.response.user.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceResponse {
    Long attendanceId;
    String checkIn;
    String checkOut;
    Long workHour;
    String employeeName;
}
