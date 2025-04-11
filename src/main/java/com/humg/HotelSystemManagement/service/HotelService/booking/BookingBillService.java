package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.dto.request.booking.bookingBill.BookingBillRequest;
import com.humg.HotelSystemManagement.dto.response.booking.bookingBill.BookingBillResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingBill;
import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingBillService {
    BookingBillRepository bookingBillRepository;
    BookingRepository bookingRepository;

    @Transactional
    public BookingBillResponse createBill (BookingBillRequest request) {
        if(request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        String bookingId = request.getBookingId();

        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        //Properties to update bill
        var grandTotal = booking.getGrandTotal();
        var issueDate = LocalDate.now();
        var paymentDate = LocalDate.now().plusDays(1);

        BookingBill bookingBill = BookingBill.builder()
                .booking(booking)
                .grandTotal(grandTotal)
                .issueDate(issueDate)
                .paymentDate(paymentDate)
                .build();

        changeBookingStatus(bookingId);

        var result = bookingBillRepository.save(bookingBill);

        return BookingBillResponse.builder()
                .bookingBillId(result.getBookingBillId())
                .bookingId(result.getBooking().getBookingId())
                .grandTotal(result.getGrandTotal())
                .issueDate(result.getIssueDate())
                .paymentDate(result.getPaymentDate())
                .build();
    }

    public void changeBookingStatus(String bookingId) {
        BookingStatus newStatus = BookingStatus.WAITING_PAYMENT;
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.setBookingStatus(newStatus);
        //Save booking và sử dụng cho bookingBill
        bookingRepository.save(booking);
    }
}
