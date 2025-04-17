package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
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
    APIResponse<BookingResponse> findBookingById(@PathVariable("bookingId") String bookingId) {
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.getBookingById(bookingId))
                .message("Get booking successfully")
                .build();
    }

    @GetMapping("/list/{customerId}")
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
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingService.deleteBooking(id);
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }
}

