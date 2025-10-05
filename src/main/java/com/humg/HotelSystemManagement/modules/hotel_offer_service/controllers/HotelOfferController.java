package com.humg.HotelSystemManagement.modules.hotel_offer_service.controllers;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests.HotelOfferRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses.HotelOfferResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.services.HotelOfferService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/offer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HotelOfferController {
    HotelOfferService hotelService;

    @PostMapping("/create")
    APIResponse<HotelOfferResponse> create(@RequestBody HotelOfferRequest request){
        return APIResponse.<HotelOfferResponse>builder()
                .result(hotelService.create(request))
                .message("Create permission successfully")
                .build();
    }

    @GetMapping("/list/")
    APIResponse<Page<HotelOfferResponse>> getAllHotelOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<Page<HotelOfferResponse>>builder()
                .result(hotelService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    @DeleteMapping("/del/{serviceName}")
    APIResponse<String> delete(@PathVariable("serviceName") String serviceName){
        hotelService.delete(serviceName);

        return APIResponse.<String>builder()
                .message("Delete permission " + serviceName + " successfully")
                .build();
    }
}
