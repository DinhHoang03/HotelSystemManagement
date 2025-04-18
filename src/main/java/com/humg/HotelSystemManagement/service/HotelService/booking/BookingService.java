package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.dto.request.booking.BookingRequest;
import com.humg.HotelSystemManagement.dto.response.booking.BookingResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingItems.BookingItemResponse;
import com.humg.HotelSystemManagement.dto.response.booking.bookingRoom.BookingRoomResponse;
import com.humg.HotelSystemManagement.dto.response.room.RoomResponse;
import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.entity.enums.PaymentStatus;
import com.humg.HotelSystemManagement.entity.enums.RoomStatus;
import com.humg.HotelSystemManagement.entity.roomManagerment.Room;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
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

    /**
    @Transactional
    public BookingResponse create(BookingRequest request){
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
            var bookingRoomResponse = bookingRoomService.createOrder(roomRequest);
            BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomResponse.getBookingRoomId())
                    .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
            bookingRoom.setBooking(savedBooking);
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
            bookingItemResponses = bookingItemsService.createOrders(bookingItemRequest);
            bookingItemList = bookingItemsRepository.findAllById(
                    bookingItemResponses.stream()
                            .map(BookingItemResponse::getBookingItemId)
                            .toList()
            );
            bookingItemList.forEach(bi -> bi.setBooking(savedBooking));
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

    /*
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

                .bookingRooms(bookingRoomResponses)
                .build();

    }
*/
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
//            if (bookingRoomList.size() != bookingRoomIds.size()) {
//                throw new AppException(AppErrorCode.INVALID_BOOKING_ROOM_ID);
//            }
            bookingRoomList.forEach(br -> System.out.println("BookingRoom ID: " + br.getBookingRoomId() + ", Rooms: " + br.getRooms()));

            for (BookingRoom bookingRoom : bookingRoomList) {
                bookingRoom.setBooking(savedBooking);
                bookingRoom.setUsername(null); // Sửa từ setUsername thành setUserId
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
//            if (bookingItemList.size() != bookingItemIds.size()) {
//                throw new AppException(AppErrorCode.INVALID_BOOKING_ITEM_ID); // Sửa mã lỗi
//            }

            for (BookingItems bookingItem : bookingItemList) {
                bookingItem.setBooking(savedBooking);
                bookingItem.setUsername(null); // Sửa từ setUsername thành setUserId
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
