package com.humg.HotelSystemManagement.modules.booking_service.resources.responses;

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
