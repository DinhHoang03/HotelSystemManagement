package com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingItems;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemResponse {
    String bookingItemId;
    String hotelOffer;
    int quantity;
    Long totalItemsPrice;
}
