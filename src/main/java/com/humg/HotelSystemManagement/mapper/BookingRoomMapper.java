package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.bookingRoom.BookingRoomResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingRoomMapper {
    BookingRoom toBookingRoom(BookingRoomRequest request);
    BookingRoomResponse toBookingRoomResponse(BookingRoom bookingRoom);
}
