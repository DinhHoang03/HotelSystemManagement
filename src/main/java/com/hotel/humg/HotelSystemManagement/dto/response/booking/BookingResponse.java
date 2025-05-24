package com.hotel.humg.HotelSystemManagement.dto.response.booking;

import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
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
    String customerName;
    List<BookingRoomResponse> bookingRooms;
    List<BookingItemResponse> bookingItems;
}
