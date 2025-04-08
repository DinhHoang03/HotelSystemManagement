package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.humg.HotelSystemManagement.dto.request.booking.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
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
        var bookingRoomRequest = request.getBookingRoomRequests();
        var customerIdRequest = request.getCustomerId();

        //Lấy thông tin khách hàng
        var customer = customerRepository.findById(customerIdRequest)
                .orElseThrow(
                        () -> new AppException(AppErrorCode.USER_NOT_EXISTED)
                );

        Booking booking = Booking.builder()
                .bookingDate(LocalDate.now())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .customer(customer)
                .totalRoomPrice(0L)
                .totalBookingServicePrice(0L)
                .grandTotal(0L)
                .bookingRooms(new ArrayList<>())
                .bookingItems(new ArrayList<>())
                .build();

        var savedBooking = bookingRepository.save(booking);

        //Tạo danh sách BookingRoom(Cách xử lý thủ công)
        List<BookingRoom> bookingRoomList = new ArrayList<>();
        List<BookingRoomResponse> bookingRoomResponses = new ArrayList<>();
        Long totalBookingRoomPrice = 0L;

        for(BookingRoomRequest roomRequest : bookingRoomRequest) {
            var bookingRoomResponse = bookingRoomService.createOrder(roomRequest, booking);
            BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomResponse.getBookingRoomId())
                    .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
            //bookingRoom.setBooking(savedBooking);
            bookingRoomList.add(bookingRoom);
            bookingRoomResponses.add(bookingRoomResponse);
            totalBookingRoomPrice += bookingRoomResponse.getTotalRoomAmount();
        }
        //bookingRoomRepository.saveAll(bookingRoomList);

        //Tạo BookingItems(Cách xử lý 1 lần 1 hàm)
        List<BookingItems> bookingItemList = new ArrayList<>();
        List<BookingItemResponse> bookingItemResponses = new ArrayList<>();
        Long totalBookingServicePrice = 0L;

        if(bookingItemRequest != null && !bookingItemRequest.isEmpty()) {
            bookingItemResponses = bookingItemsService.createOrders(bookingItemRequest, savedBooking);
            bookingItemList = bookingItemsRepository.findAllById(
                    bookingItemResponses.stream()
                            .map(BookingItemResponse::getBookingItemId)
                            .toList()
            );
            //bookingItemList.forEach(bi -> bi.setBooking(savedBooking));
            totalBookingServicePrice = bookingItemResponses.stream().mapToLong(
                    BookingItemResponse::getTotalItemsPrice
            ).sum();
            //bookingItemsRepository.saveAll(bookingItemList);
        }

        // Cập nhật Booking với bookingRooms, bookingItems và tổng tiền
        savedBooking.getBookingRooms().clear(); // Xóa danh sách cũ (nếu cần)
        savedBooking.getBookingRooms().addAll(bookingRoomList); // Thêm danh sách mới
        savedBooking.getBookingItems().clear(); // Xóa danh sách cũ (nếu cần)
        savedBooking.getBookingItems().addAll(bookingItemList); // Thêm danh sách mới
        savedBooking.setTotalRoomPrice(totalBookingRoomPrice);
        savedBooking.setTotalBookingServicePrice(totalBookingServicePrice);
        savedBooking.setGrandTotal(totalBookingRoomPrice + totalBookingServicePrice);
        var finalBooking = bookingRepository.save(savedBooking); // Lưu lần cuối
        //Tạo booking
        /**
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
        */
        //bookingRoomList.forEach(br -> br.setBooking(booking));
        //bookingItemList.forEach((bi -> bi.setBooking(booking)));

        //var result = bookingRepository.save(booking);

        return BookingResponse.builder()
                .bookingId(finalBooking.getBookingId())
                .bookingDate(finalBooking.getBookingDate())
                .bookingStatus(finalBooking.getBookingStatus().toString())
                .paymentStatus(finalBooking.getPaymentStatus().toString())
                .totalRoomPrice(finalBooking.getTotalRoomPrice())
                .totalBookingServicePrice(finalBooking.getTotalBookingServicePrice())
                .grandTotal(finalBooking.getGrandTotal())
                .customerName(finalBooking.getCustomer().getName())
                /**
                .bookingItems(finalBooking.getBookingItems()
                        .stream()
                        .map(
                                bookingItems -> BookingItemResponse.builder()
                                        .hotelOffer(bookingItems.getHotelOffers().getServiceTypes())
                                        .quantity(bookingItems.getQuantity())
                                        .totalItemsPrice(bookingItems.getTotalItemsPrice())
                                        .build()
                        ).collect(Collectors.toList()))
                */
                .bookingItems(bookingItemResponses)
                 /**
                .bookingRooms(finalBooking.getBookingRooms()
                        .stream()
                        .map(bookingRoom -> BookingRoomResponse.builder()
                                .checkInDate(bookingRoom.getCheckInDate())
                                .checkOutDate(bookingRoom.getCheckOutDate())
                                .rooms(
                                        bookingRoom.getRooms()
                                                .stream()
                                                .map(Room::getRoomNumber)
                                                .collect(Collectors.toList())
                                )
                                .totalRoomAmount(bookingRoom.getTotalRoomAmount())
                                .build()
                        ).collect(Collectors.toList())
                )
                 */
                .bookingRooms(bookingRoomResponses)
                .build();

    }
    public void deleteBooking(String id) {
        var booking = bookingRepository.findById(id).orElseThrow(() -> new AppException(AppErrorCode.REQUEST_IS_NULL));
        bookingRepository.delete(booking);
    }
}
