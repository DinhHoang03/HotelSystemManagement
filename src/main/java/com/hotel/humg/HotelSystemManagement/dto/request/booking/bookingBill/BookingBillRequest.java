package com.hotel.humg.HotelSystemManagement.dto.request.booking.bookingBill;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingBillRequest {
    String bookingId;
}
