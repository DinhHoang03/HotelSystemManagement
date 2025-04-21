package com.humg.HotelSystemManagement.dto.response.humanEntity.employee;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
