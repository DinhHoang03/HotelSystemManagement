package com.humg.HotelSystemManagement.dto.response.bookingItems;

import com.humg.HotelSystemManagement.entity.totalServices.HotelOffers;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemResponse {
    Long bookingItemId;
    String hotelOffer;
    int quantity;
    Long totalItemsPrice;
}
