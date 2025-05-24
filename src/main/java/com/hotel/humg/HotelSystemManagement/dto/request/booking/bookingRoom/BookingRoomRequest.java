package com.hotel.humg.HotelSystemManagement.dto.request.booking.bookingRoom;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRoomRequest {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate checkInDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate checkOutDate;
    //Long totalRoomAmount;
    List<String> roomNumbers;
}
