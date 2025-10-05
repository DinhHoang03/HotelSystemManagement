package com.humg.HotelSystemManagement.modules.booking_service.mappers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingItemResponse;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingItemsMapper {
    BookingItemResponse toBookingItemResponse(BookingItems items);
}
