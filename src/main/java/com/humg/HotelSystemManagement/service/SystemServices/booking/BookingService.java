package com.humg.HotelSystemManagement.service.SystemServices.booking;

import com.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.humg.HotelSystemManagement.dto.request.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.humg.HotelSystemManagement.dto.response.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.entity.enums.PaymentStatus;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService{
    BookingRepository bookingRepository;
    BookingItemsService bookingItemsService;
    BookingRoomService bookingRoomService;
    CustomerRepository customerRepository;
    BookingRoomRepository bookingRoomRepository;
    BookingItemsRepository bookingItemsRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request){
        if(request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        var bookingItemRequest = request.getBookingItemRequests();
        var bookingRoomRequest = request.getBookingRoomRequest();
        var customerIdRequest = request.getCustomerId();

        //Lấy thông tin khách hàng
        var customer = customerRepository.findById(customerIdRequest)
                .orElseThrow(
                        () -> new AppException(AppErrorCode.USER_NOT_EXISTED)
                );

        //Tạo danh sách BookingRoom
        List<BookingRoom> bookingRoomList = new ArrayList<>();
        Long totalBookingRoomPrice = 0L;
        for(BookingRoomRequest roomRequest : request.getBookingRoomRequest()) {
            var bookingRoomResponse = bookingRoomService.create(roomRequest);
            BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomResponse.getBookingRoomId())
                    .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
            bookingRoomList.add(bookingRoom);
            totalBookingRoomPrice += bookingRoomResponse.getTotalRoomAmount();
        }

        //Tạo BookingItems
        List<BookingItems> bookingItemList = new ArrayList<>();
        Long totalBookingServicePrice = 0L;

        if(request.getBookingItemRequests() != null && !request.getBookingItemRequests().isEmpty()) {
            var bookingItemResponse = bookingItemsService.createOrders(request.getBookingItemRequests());
            bookingItemList = bookingItemsRepository.findAllById(
                    bookingItemResponse.stream()
                            .map(BookingItemResponse::getBookingItemId)
                            .toList()
            );
            totalBookingServicePrice = bookingItemResponse.stream().mapToLong(
                    BookingItemResponse::getTotalItemsPrice
            ).sum();
        }

        //Tạo booking
        Booking booking = Booking.builder()
                .bookingDate(LocalDate.now())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .totalRoomPrice(totalBookingRoomPrice)
                .totalBookingServicePrice(totalBookingServicePrice)
                .grandTotal(totalBookingRoomPrice + totalBookingServicePrice)
                .customer(customer)
                .bookingItems(bookingItemList)
                .bookingRooms(bookingRoomList)
                .build();

        bookingRoomList.forEach(br -> br.setBooking(booking));
        bookingItemList.forEach((bi -> bi.setBooking(booking)));

        var result = bookingRepository.save(booking);

        return BookingResponse.builder()
                .bookingId(result.getBookingId())
                .bookingDate(result.getBookingDate())
                .bookingStatus(result.getBookingStatus().toString())
                .paymentStatus(result.getPaymentStatus().toString())
                .totalRoomPrice(result.getTotalRoomPrice())
                .totalBookingServicePrice(result.getTotalBookingServicePrice())
                .grandTotal(result.getGrandTotal())
                .customerName(result.getCustomer().getName())
                .build();

    }
}
