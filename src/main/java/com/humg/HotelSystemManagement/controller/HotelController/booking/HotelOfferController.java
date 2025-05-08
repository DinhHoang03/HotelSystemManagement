package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.room.hotelService.HotelOfferRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.room.hotelService.HotelOfferResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.HotelOfferService;
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

    @GetMapping("/list/{page}/{size}")
    APIResponse<Page<HotelOfferResponse>> getAllHotelOffers(
            @PathVariable("page") int page,
            @PathVariable("size") int size
    ){
        return APIResponse.<Page<HotelOfferResponse>>builder()
                .result(hotelService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    @DeleteMapping("/del/{serviceName}")
    APIResponse delete(@RequestParam("serviceName") String serviceName){
        hotelService.delete(serviceName);

        return APIResponse.builder()
                .message("Delete permission " + serviceName + " successfully")
                .build();
    }
}
