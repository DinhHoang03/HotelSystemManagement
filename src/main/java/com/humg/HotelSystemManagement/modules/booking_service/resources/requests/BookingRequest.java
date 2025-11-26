package com.humg.HotelSystemManagement.modules.booking_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    String customerId;
    List<String> bookingRoomIds;
    List<String> bookingItemIds;
}
