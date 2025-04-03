package com.humg.HotelSystemManagement.dto.request.bookingRoom;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRoomRequest {
    LocalDate checkInDate;
    LocalDate checkOutDate;
    Long totalRoomAmount;
    List<String> roomNumbers;
}
