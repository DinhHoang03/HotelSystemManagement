package com.humg.HotelSystemManagement.dto.response.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    String paymentOrderId;
}
