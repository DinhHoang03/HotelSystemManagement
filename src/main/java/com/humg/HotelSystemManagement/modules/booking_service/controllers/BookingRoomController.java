package com.humg.HotelSystemManagement.modules.booking_service.controllers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingRoomRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingRoomResponse;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingRoomController {
    BookingRoomService bookingRoomService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
     APIResponse<BookingRoomResponse> createBooking(
             @RequestBody BookingRoomRequest request,
             @AuthenticationPrincipal Jwt principal
    ) {
        String username = principal.getSubject(); //Lấy username từ jwt trong claím set subject(check trong authenticationService là ra)
        return APIResponse.<BookingRoomResponse>builder()
                .result(bookingRoomService.createOrder(request, username))
                .message("Create order room success")
                .build();
    }

    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingRoomService.delete(id);
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }
}