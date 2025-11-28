package com.humg.HotelSystemManagement.modules.booking_service.resources.responses;

import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRoomResponse {
    String bookingRoomId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    Long totalRoomAmount;
    List<RoomResponse> rooms;
}
