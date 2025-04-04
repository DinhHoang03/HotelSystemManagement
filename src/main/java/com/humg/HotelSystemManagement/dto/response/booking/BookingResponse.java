package com.humg.HotelSystemManagement.dto.response.booking;

import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String bookingId;
    LocalDate bookingDate;
    String bookingStatus;
    String paymentStatus;
    Long totalRoomPrice;
    Long totalBookingServicePrice;
    Long grandTotal;
    String customerId;
    List<BookingRoom> bookingRooms;
    List<BookingItems> bookingItems;
}
