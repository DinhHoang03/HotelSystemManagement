package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingRoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
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
    APIResponse<String> cancelBooking(@PathVariable("id") String id) {
        bookingRoomService.delete(id);
        return APIResponse.<String>builder()
                .message("Cancel order room successfully")
                .build();
    }
}