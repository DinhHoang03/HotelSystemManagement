package com.humg.HotelSystemManagement.dto.request.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingItems.BookingItemRequest;
import com.humg.HotelSystemManagement.dto.request.booking.bookingRoom.BookingRoomRequest;
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
//    List<BookingRoomRequest> bookingRoomRequests;
//    List<BookingItemRequest> bookingItemRequests;
    List<String> bookingRoomIds;
    List<String> bookingItemIds;
}
