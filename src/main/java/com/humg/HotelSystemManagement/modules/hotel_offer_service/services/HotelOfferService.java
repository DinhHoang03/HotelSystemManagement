package com.humg.HotelSystemManagement.modules.hotel_offer_service.services;

import com.humg.HotelSystemManagement.utils.NormalizeString;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests.HotelOfferRequest;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses.HotelOfferResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.mappers.HotelOfferMapper;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.utils.interfaces.ISimpleCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HotelOfferService implements ISimpleCRUDService<HotelOfferResponse, HotelOfferRequest, String> {
    HotelOffersRepository hotelServiceRepository;
    HotelOfferMapper hotelOfferMapper;
    NormalizeString normalizeString;

    @Override
    public HotelOfferResponse create(HotelOfferRequest request) {
        if(request == null){
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        var serviceType = normalizeString.normalizedString(request.getServiceType());

        if(hotelServiceRepository.existsByServiceTypes(serviceType)){
            throw new AppException(AppErrorCode.OBJECT_EXISTED);
        }

        var hotelService = HotelOffers.builder()
                .serviceTypes(serviceType)
                .price(request.getPrice())
                .build();

        var result = hotelServiceRepository.save(hotelService);

        return HotelOfferResponse.builder()
                .serviceType(hotelService.getServiceTypes())
                .price(hotelService.getPrice())
                .build();
    }

    @Override
    public Page<HotelOfferResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HotelOffers> result = hotelServiceRepository.findAll(pageable);

        return result.map(hotelOffers ->
                HotelOfferResponse.builder()
                        .serviceType(hotelOffers.getServiceTypes())
                        .price(hotelOffers.getPrice())
                        .build());
    }

    @Override
    public HotelOfferResponse getById(String id) {
        var hotelService = hotelServiceRepository.findById(id).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        return hotelOfferMapper.toHotelOfferResponse(hotelService);
    }

    @Override
    public HotelOfferResponse update(String id, HotelOfferRequest request) {
        return null;
    }

    @Override
    public void delete(String serviceType) {
        var hotelService = hotelServiceRepository.findByServiceTypes(serviceType).orElseThrow(
                () -> new AppException(AppErrorCode.OBJECT_IS_NULL)
        );

        hotelServiceRepository.delete(hotelService);
    }
}
