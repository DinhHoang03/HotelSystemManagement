package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humg.HotelSystemManagement.configuration.payment.ZaloPayConfig;
import com.humg.HotelSystemManagement.dto.request.booking.bookingBill.BookingBillRequest;
import com.humg.HotelSystemManagement.dto.response.booking.bookingBill.BookingBillResponse;
import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.booking.BookingBill;

import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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

        // Kiểm tra nếu booking đã có bill thì trả về bill đó
        if (booking.getBookingBill() != null) {
            BookingBill existingBill = booking.getBookingBill();
            return BookingBillResponse.builder()
                    .bookingBillId(existingBill.getBookingBillId())
                    .bookingId(existingBill.getBooking().getBookingId())
                    .grandTotal(existingBill.getGrandTotal())
                    .issueDate(existingBill.getIssueDate())
                    .paymentDate(existingBill.getPaymentDate())
                    .build();
        }

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

    public Page<BookingBillResponse> getAllBookingBills(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<BookingBill> result = bookingBillRepository.findCustomerIdByBookingBillId(customerId, pageable);
        if(result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<BookingBillResponse> response = result.map(
                bookingBill -> {
                    return BookingBillResponse.builder()
                            .bookingBillId(bookingBill.getBookingBillId())
                            .issueDate(bookingBill.getIssueDate())
                            .grandTotal(bookingBill.getGrandTotal())
                            .paymentDate(bookingBill.getPaymentDate())
                            .bookingId(bookingBill.getBooking().getBookingId())
                            .build();
                }
        );
        return response;
    }

    @Transactional
    public void deleteBill(String bookingBillId, String username) {
        // Tìm booking bill
        var bookingBill = bookingBillRepository.findById(bookingBillId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Booking booking = bookingRepository.findById(bookingBill.getBooking().getBookingId())
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.getBookingBill();
        booking.setBookingBill(null);
        bookingRepository.save(booking);

        // Kiểm tra quyền truy cập
        if (!bookingBill.getBooking().getCustomer().getUsername().equals(username)) {
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }

        // Cập nhật lại trạng thái của booking (chuyển từ WAITING_PAYMENT về CONFIRMED)
        //booking = bookingBill.getBooking();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Xóa booking bill
        bookingBillRepository.delete(bookingBill);
    }


}
