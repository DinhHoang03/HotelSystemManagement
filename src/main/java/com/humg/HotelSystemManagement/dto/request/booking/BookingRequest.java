package com.humg.HotelSystemManagement.dto.request.booking;

import com.humg.HotelSystemManagement.dto.request.bookingItems.BookingItemRequest;
import com.humg.HotelSystemManagement.dto.request.bookingRoom.BookingRoomRequest;
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
    List<BookingRoomRequest> bookingRoomRequest;
    List<BookingItemRequest> bookingItemRequests;
}
