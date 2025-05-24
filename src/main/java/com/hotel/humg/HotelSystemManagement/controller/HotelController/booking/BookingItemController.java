package com.hotel.humg.HotelSystemManagement.controller.HotelController.booking;

import com.hotel.humg.HotelSystemManagement.dto.request.booking.bookingItems.BookingItemRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.APIResponse;
import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.hotel.humg.HotelSystemManagement.service.HotelService.booking.BookingItemsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingItemController {
    BookingItemsService bookingItemsService;

    @PostMapping("/create")
    public APIResponse<BookingItemResponse> createOrder(
            @RequestBody BookingItemRequest request,
            @AuthenticationPrincipal Jwt principal
    ) {
        String username = principal.getSubject();
        var result = bookingItemsService.createOrder(request, username);
        return APIResponse.<BookingItemResponse>builder()
                .result(result)
                .message("Create order successfully")
                .build();
    }

    @DeleteMapping("/del/{id}")
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingItemsService.deleteBookingItems(id);
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }
}
