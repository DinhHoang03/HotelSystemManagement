package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.bookingItems.BookingItemRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.service.SystemServices.booking.BookingItemsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking-item")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemController {
    BookingItemsService bookingItemsService;

    @PostMapping("/create")
    public APIResponse<BookingItemResponse> createOrder(@RequestBody BookingItemRequest request) {
        var result = bookingItemsService.createOrder(request);
        return APIResponse.<BookingItemResponse>builder()
                .result(result)
                .message("Create order successfully")
                .build();
    }
}
