package com.humg.HotelSystemManagement.modules.booking_service.controllers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingRoomRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingRoomResponse;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
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
        String username = principal.getSubject();
        return APIResponse.<BookingRoomResponse>builder()
                .result(bookingRoomService.createOrder(request, username))
                .message("Create order room success")
                .build();
    }

    // --- API MỚI: LẤY GIỎ HÀNG (IN_CART) ---
    @GetMapping("/cart")
    @PreAuthorize("hasAuthority('BOOKING_VIEW')")
    APIResponse<Page<BookingRoomResponse>> getMyCart(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var username = principal.getSubject();
        return APIResponse.<Page<BookingRoomResponse>>builder()
                .result(bookingRoomService.getMyCart(username, page, size))
                .message("Get cart rooms successfully")
                .build();
    }
    // ----------------------------------------

    @GetMapping("/get-all")//Phần admin
    @PreAuthorize("hasAuthority('BOOKING_VIEW')")
    APIResponse<Page<BookingRoomResponse>> getAllBookingRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<BookingRoomResponse>>builder()
                .result(bookingRoomService.getAll(page, size))
                .message("Get all booking rooms successfully")
                .build();
    }

    @GetMapping("/list") //Phần của khách hàng (LỊCH SỬ)
    APIResponse<Page<BookingRoomResponse>> getAllByUsername(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var username = principal.getSubject();
        return APIResponse.<Page<BookingRoomResponse>>builder()
                .result(bookingRoomService.getAllByUsername(username, page, size))
                .build();
    }

    @GetMapping("/info/{id}") //Phần của khách hàng
    APIResponse<BookingRoomResponse> getBookingRoomById(@PathVariable("id") String id) {
        return APIResponse.<BookingRoomResponse>builder()
                .result(bookingRoomService.getById(id))
                .message("Get booking room detail successfully")
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