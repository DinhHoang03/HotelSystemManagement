package com.humg.HotelSystemManagement.modules.booking_service.controllers;

import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingItemRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingItemResponse;
import com.humg.HotelSystemManagement.modules.booking_service.services.BookingItemsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
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

    @GetMapping("/get-all")
    @PreAuthorize("hasAuthority('BOOKING_VIEW')")
    APIResponse<Page<BookingItemResponse>> getAllBookingItemsGlobal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<BookingItemResponse>>builder()
                .result(bookingItemsService.getAll(page, size))
                .message("Get all booking items successfully")
                .build();
    }

    // API 2: User xem danh sách món mình đã order (Dựa trên Token)
    @GetMapping("/my-list")
    APIResponse<Page<BookingItemResponse>> getMyBookingItems(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var username = principal.getSubject();
        return APIResponse.<Page<BookingItemResponse>>builder()
                .result(bookingItemsService.getAllByUsername(username, page, size))
                .message("Get my booking items successfully")
                .build();
    }

    // API 3: Xem chi tiết 1 item cụ thể
    @GetMapping("/info/{id}")
    APIResponse<BookingItemResponse> getBookingItemById(@PathVariable("id") String id) {
        return APIResponse.<BookingItemResponse>builder()
                .result(bookingItemsService.getById(id))
                .message("Get booking item detail successfully")
                .build();
    }

    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingItemsService.deleteBookingItems(id);
        return APIResponse.<String>builder()
                .message("Cancel order service successfully")
                .build();
    }
}
