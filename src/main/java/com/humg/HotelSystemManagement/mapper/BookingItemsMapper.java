package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingItemsMapper {
    BookingItemResponse toBookingItemResponse(BookingItems items);
}
