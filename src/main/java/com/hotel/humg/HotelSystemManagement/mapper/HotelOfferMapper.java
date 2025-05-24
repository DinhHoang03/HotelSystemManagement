package com.hotel.humg.HotelSystemManagement.mapper;

import com.hotel.humg.HotelSystemManagement.dto.request.room.hotelService.HotelOfferRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.room.hotelService.HotelOfferResponse;
import com.hotel.humg.HotelSystemManagement.entity.totalServices.HotelOffers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelOfferMapper {
    HotelOffers toHotelOffer(HotelOfferRequest request);
    HotelOfferResponse toHotelOfferResponse(HotelOffers hotelService);
}
