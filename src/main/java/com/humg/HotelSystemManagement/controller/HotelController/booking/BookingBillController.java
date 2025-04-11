package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingBill.BookingBillRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingBill.BookingBillResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingBillService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingBillController {
    BookingBillService bookingBillService;

    @GetMapping("/create")
    APIResponse<BookingBillResponse> createBookingBill(@RequestBody BookingBillRequest request) throws Exception {
        var bill = bookingBillService.createBill(request);
        return APIResponse.<BookingBillResponse>builder()
                .result(bill)
                .message("Create bill successfully")
                .build();
    }
}
