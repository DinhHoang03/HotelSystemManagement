package com.humg.HotelSystemManagement.modules.booking_service.resources.requests;

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
