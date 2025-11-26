package com.humg.HotelSystemManagement.modules.booking_service.controllers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingResponse;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    APIResponse<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt principal
    ){
        var username = principal.getSubject();
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(request, username))
                .message("Create booking successfully!")
                .build();
    }

    @GetMapping("/info/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING_VIEW')")
    APIResponse<BookingResponse> findBookingById(@PathVariable("bookingId") String bookingId) {
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.getBookingById(bookingId))
                .message("Get booking successfully")
                .build();
    }

    @GetMapping("/list/{customerId}")
    @PreAuthorize("hasAuthority('BOOKING_VIEW')")
    APIResponse<Page<BookingResponse>> getAllBooking(
            @PathVariable("customerId") String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<BookingResponse>>builder()
                .result(bookingService.getAllBookingByUserId(customerId, page, size))
                .message("Get all bookings successfully")
                .build();
    }

    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('BOOKING_CANCEL')")
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingService.deleteBooking(id);
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }

    @PostMapping("/{bookingId}/add-service")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    public APIResponse<BookingResponse> addService(
            @PathVariable String bookingId,
            @RequestParam String offerId,
            @RequestParam int quantity
    ) {
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.addServiceToBooking(bookingId, offerId, quantity))
                .message("Service added successfully")
                .build();
    }
}