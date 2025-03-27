package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.hotelService.HotelOfferRequest;
import com.humg.HotelSystemManagement.dto.response.hotelServiceResponse.HotelOfferResponse;
import com.humg.HotelSystemManagement.entity.totalServices.HotelOffers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelOfferMapper {
    HotelOffers toHotelOffer(HotelOfferRequest request);
    HotelOfferResponse toHotelOfferResponse(HotelOffers hotelService);
}
