package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking-rooms")
public class BookingRoomController {

    @Autowired
    private BookingRoomService bookingRoomService;

    @PostMapping("/create")
     APIResponse<BookingRoomResponse> createBooking(@RequestBody BookingRoomRequest request) {
        return APIResponse.<BookingRoomResponse>builder()
                .result(bookingRoomService.create(request))
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