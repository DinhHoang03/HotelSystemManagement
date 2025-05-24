package com.hotel.humg.HotelSystemManagement.mapper;

import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.hotel.humg.HotelSystemManagement.entity.booking.BookingItems;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingItemsMapper {
    BookingItemResponse toBookingItemResponse(BookingItems items);
}
