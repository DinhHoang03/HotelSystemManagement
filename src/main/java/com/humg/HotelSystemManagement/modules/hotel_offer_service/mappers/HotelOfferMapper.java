package com.humg.HotelSystemManagement.modules.hotel_offer_service.mappers;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests.HotelOfferRequest;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses.HotelOfferResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelOfferMapper {
    HotelOffers toHotelOffer(HotelOfferRequest request);
    HotelOfferResponse toHotelOfferResponse(HotelOffers hotelService);
}
