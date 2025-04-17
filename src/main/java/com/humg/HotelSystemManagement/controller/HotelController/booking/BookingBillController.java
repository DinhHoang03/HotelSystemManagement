package com.humg.HotelSystemManagement.controller.HotelController.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingBill.BookingBillRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingBill.BookingBillResponse;
import com.humg.HotelSystemManagement.service.HotelService.booking.BookingBillService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingBillController {
    BookingBillService bookingBillService;

    @PostMapping("/create")
    APIResponse<BookingBillResponse> createBookingBill(@RequestBody BookingBillRequest request) {
        var bill = bookingBillService.createBill(request);
        return APIResponse.<BookingBillResponse>builder()
                .result(bill)
                .message("Create bill successfully")
                .build();
    }

    @GetMapping("list/{customerId}")
    APIResponse<Page<BookingBillResponse>> getAllBookingBill(
            @PathVariable("customerId") String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<BookingBillResponse>>builder()
                .result(bookingBillService.getAllBookingBills(customerId, page, size))
                .message("Get all bills successfully")
                .build();
    }

    @DeleteMapping("/del/{billId}")
    APIResponse<Void> deleteBookingBill(@PathVariable("billId") String billId,
                                       @AuthenticationPrincipal Jwt principal) {
        String username = principal.getSubject();
        bookingBillService.deleteBill(billId, username);
        
        return APIResponse.<Void>builder()
                .message("Bill deleted successfully")
                .build();
    }

}
