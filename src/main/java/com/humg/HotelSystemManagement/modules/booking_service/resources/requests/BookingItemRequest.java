package com.humg.HotelSystemManagement.modules.booking_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemRequest {
    String hotelOffer;
    int quantity;
}
