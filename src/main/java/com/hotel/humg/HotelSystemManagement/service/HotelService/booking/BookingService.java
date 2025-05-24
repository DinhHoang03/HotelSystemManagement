package com.hotel.humg.HotelSystemManagement.service.HotelService.booking;

import com.hotel.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.hotel.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
import com.hotel.humg.HotelSystemManagement.entity.booking.Booking;
import com.hotel.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.hotel.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.hotel.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.hotel.humg.HotelSystemManagement.entity.enums.PaymentStatus;
import com.hotel.humg.HotelSystemManagement.entity.enums.RoomStatus;
import com.hotel.humg.HotelSystemManagement.entity.roomManagerment.Room;
import com.hotel.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.hotel.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.hotel.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
import com.hotel.humg.HotelSystemManagement.repository.booking.BookingRepository;
import com.hotel.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
import com.hotel.humg.HotelSystemManagement.repository.User.CustomerRepository;
import com.hotel.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    RoomRepository roomRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        if (request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        var customerId = request.getCustomerId();
        var bookingRoomIds = request.getBookingRoomIds();
        var bookingItemIds = request.getBookingItemIds();

        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

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

        // Xử lý BookingRoom
        List<BookingRoom> bookingRoomList = new ArrayList<>();
        List<BookingRoomResponse> bookingRoomResponses = new ArrayList<>();
        Long totalBookingRoomPrice = 0L;

        if (bookingRoomIds != null && !bookingRoomIds.isEmpty()) {
            bookingRoomList = bookingRoomRepository.findByUsernameAndBookingRoomIdIn(username, bookingRoomIds);

            bookingRoomList.forEach(br -> System.out.println("BookingRoom ID: " + br.getBookingRoomId() + ", Rooms: " + br.getRooms()));

            for (BookingRoom bookingRoom : bookingRoomList) {
                bookingRoom.setBooking(savedBooking);
                bookingRoom.setBookingStatus(BookingStatus.IN_PROGRESS);
                totalBookingRoomPrice += bookingRoom.getTotalRoomAmount();

                bookingRoomResponses.add(BookingRoomResponse.builder()
                        .bookingRoomId(bookingRoom.getBookingRoomId())
                        .checkInDate(bookingRoom.getCheckInDate())
                        .checkOutDate(bookingRoom.getCheckOutDate())
                        .totalRoomAmount(bookingRoom.getTotalRoomAmount())
                        .rooms(bookingRoom.getRooms()
                                .stream()
                                .map(Room::getRoomNumber)
                                .collect(Collectors.toList()))
                        .build());
            }
            bookingRoomRepository.saveAll(bookingRoomList);
        }

        // Xử lý BookingItem (tách ra ngoài if của BookingRoom)
        List<BookingItems> bookingItemList = new ArrayList<>();
        List<BookingItemResponse> bookingItemResponses = new ArrayList<>();
        Long totalBookingServicePrice = 0L;

        if (bookingItemIds != null && !bookingItemIds.isEmpty()) {
            bookingItemList = bookingItemsRepository.findByUsernameAndBookingItemIdIn(username, bookingItemIds);

            for (BookingItems bookingItem : bookingItemList) {
                bookingItem.setBooking(savedBooking);
                totalBookingServicePrice += bookingItem.getTotalItemsPrice();

                bookingItemResponses.add(BookingItemResponse.builder()
                        .bookingItemId(bookingItem.getBookingItemId())
                        .hotelOffer(bookingItem.getHotelOffers().getServiceTypes())
                        .quantity(bookingItem.getQuantity())
                        .totalItemsPrice(bookingItem.getTotalItemsPrice())
                        .build());
            }
            bookingItemsRepository.saveAll(bookingItemList); // Sửa repository
        }

        // Tính tổng và cập nhật Booking (đưa ra ngoài if)
        var grandTotal = totalBookingRoomPrice + totalBookingServicePrice;

        savedBooking.getBookingRooms().clear();
        savedBooking.getBookingRooms().addAll(bookingRoomList);
        savedBooking.getBookingItems().clear();
        savedBooking.getBookingItems().addAll(bookingItemList);
        savedBooking.setTotalRoomPrice(totalBookingRoomPrice);
        savedBooking.setTotalBookingServicePrice(totalBookingServicePrice);
        savedBooking.setGrandTotal(grandTotal);

        var finalBooking = bookingRepository.save(savedBooking);

        return BookingResponse.builder()
                .bookingId(finalBooking.getBookingId())
                .bookingDate(finalBooking.getBookingDate())
                .bookingStatus(finalBooking.getBookingStatus().toString())
                .paymentStatus(finalBooking.getPaymentStatus().toString())
                .totalRoomPrice(finalBooking.getTotalRoomPrice())
                .totalBookingServicePrice(finalBooking.getTotalBookingServicePrice())
                .grandTotal(finalBooking.getGrandTotal())
                .customerName(finalBooking.getCustomer().getName())
                .bookingItems(bookingItemResponses)
                .bookingRooms(bookingRoomResponses)
                .build();
    }

    public Page<BookingResponse> getAllBookingByUserId(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Booking> result = bookingRepository.findByCustomer_Id(customerId, pageable);
        if (result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<BookingResponse> response = result.map(booking -> {
           return BookingResponse.builder()
                   .bookingId(booking.getBookingId())
                   .bookingDate(booking.getBookingDate())
                   .bookingStatus(booking.getBookingStatus().toString())
                   .paymentStatus(booking.getPaymentStatus().toString())
                   .totalBookingServicePrice(booking.getTotalBookingServicePrice())
                   .totalRoomPrice(booking.getTotalRoomPrice())
                   .grandTotal(booking.getGrandTotal())
                   .customerName(booking.getCustomer().getName())
                   .build();
        });

        return response;
    }

    public BookingResponse getBookingById(String bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        List<BookingRoomResponse> bookingRoomResponses = booking.getBookingRooms()
                .stream()
                .map(bookingRoom -> BookingRoomResponse.builder()
                        .checkInDate(bookingRoom.getCheckInDate())
                        .checkOutDate(bookingRoom.getCheckOutDate())
                        .totalRoomAmount(bookingRoom.getTotalRoomAmount())
                        .rooms(bookingRoom.getRooms()
                                .stream()
                                .map(Room::getRoomNumber)
                                .collect(Collectors.toList())
                        ).build()
                )
                .collect(Collectors.toList());

        List<BookingItemResponse> bookingItemResponses = booking.getBookingItems()
                .stream()
                .map(bookingItems -> BookingItemResponse.builder()
                        .hotelOffer(bookingItems.getHotelOffers().getServiceTypes())
                        .quantity(bookingItems.getQuantity())
                        .totalItemsPrice(bookingItems.getTotalItemsPrice())
                        .build()
                ).collect(Collectors.toList());

        var response = BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingDate(booking.getBookingDate())
                .bookingStatus(BookingStatus.CONFIRMED.toString())
                .paymentStatus(PaymentStatus.COMPLETED.toString())
                .totalRoomPrice(booking.getTotalRoomPrice())
                .totalBookingServicePrice(booking.getTotalBookingServicePrice())
                .grandTotal(booking.getGrandTotal())
                .customerName(booking.getCustomer().getName())
                .bookingRooms(bookingRoomResponses)
                .bookingItems(bookingItemResponses)
                .build();

        return response;
    }

    public void updatePaymentStatus(String bookingId, String paymentOrderId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setPaymentOrderId(paymentOrderId);

        bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(String bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        // Duyệt qua tất cả BookingRoom
        for (BookingRoom bookingRoom : booking.getBookingRooms()) {
            // Xóa liên kết từ Room đến BookingRoom
            List<Room> rooms = bookingRoom.getRooms();
            rooms.forEach(room -> {
                room.setRoomStatus(RoomStatus.AVAILABLE);
                room.setBookingRoom(null); // Đặt booking_room_id về NULL
            });
            roomRepository.saveAll(rooms);
        }

        // Xóa Booking (sẽ xóa cả BookingRoom do cascade)
        bookingRepository.delete(booking);
    }
}
