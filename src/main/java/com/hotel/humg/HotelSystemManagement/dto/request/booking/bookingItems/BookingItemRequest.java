package com.hotel.humg.HotelSystemManagement.dto.request.booking.bookingItems;

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
