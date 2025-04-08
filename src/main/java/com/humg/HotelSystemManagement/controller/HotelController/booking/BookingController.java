package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping("/create")
    APIResponse<BookingResponse> createBooking(@RequestBody BookingRequest request){
        return APIResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(request))
                .message("Create booking successfully!")
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

