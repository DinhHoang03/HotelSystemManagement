package com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingRoom;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRoomResponse {
    String bookingRoomId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    Long totalRoomAmount;
    List<String> rooms;
}
