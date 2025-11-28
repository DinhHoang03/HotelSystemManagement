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

    // 1. TẠO ĐƠN ĐẶT PHÒNG
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

    // 2. XEM CHI TIẾT ĐƠN (Đã thêm check quyền sở hữu)
    @GetMapping("/info/{bookingId}")
    APIResponse<BookingResponse> findBookingById(
            @PathVariable("bookingId") String bookingId,
            @AuthenticationPrincipal Jwt principal // Thêm lấy Token
    ) {
        String username = principal.getSubject(); // Lấy username từ Token
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.getBookingById(bookingId, username)) // Truyền username vào Service
                .message("Get booking successfully")
                .build();
    }

    // 3. DANH SÁCH ĐƠN CỦA TÔI
    // Lưu ý: Đã đổi path thành /list để gọn hơn, không cần {customerId} trên URL nữa vì lấy từ Token rồi
    @GetMapping("/list")
    APIResponse<Page<BookingResponse>> getAllBooking(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = principal.getSubject();
        return APIResponse.<Page<BookingResponse>>builder()
                .result(bookingService.getAllBookingByUsername(username, page, size))
                .message("Get all bookings successfully")
                .build();
    }

    // 4. HỦY ĐƠN (Đã thêm check quyền sở hữu)
    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('BOOKING_CANCEL')")
    APIResponse<String> cancelBooking(
            @PathVariable("id") String id,
            @AuthenticationPrincipal Jwt principal // Thêm lấy Token
    ) {
        String username = principal.getSubject(); // Lấy username
        bookingService.deleteBooking(id, username); // Truyền vào Service
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }

    // 5. THÊM DỊCH VỤ VÀO ĐƠN (Đã thêm check quyền sở hữu)
    @PostMapping("/{bookingId}/add-service")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    public APIResponse<BookingResponse> addService(
            @PathVariable String bookingId,
            @RequestParam String offerId,
            @RequestParam int quantity,
            @AuthenticationPrincipal Jwt principal // Thêm lấy Token
    ) {
        String username = principal.getSubject(); // Lấy username
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.addServiceToBooking(bookingId, offerId, quantity, username)) // Truyền vào Service
                .message("Service added successfully")
                .build();
    }
}