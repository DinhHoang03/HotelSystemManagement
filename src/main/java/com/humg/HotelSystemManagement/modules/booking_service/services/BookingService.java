package com.humg.HotelSystemManagement.modules.booking_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingItemsRepository;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRoomRepository;
import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingRequest;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingItemResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingResponse;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingRoomResponse;
import com.humg.HotelSystemManagement.modules.customer_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.utils.enums.PaymentMethod;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import com.humg.HotelSystemManagement.utils.enums.RoomStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    UserRepository userRepository;
    BookingRoomRepository bookingRoomRepository;
    BookingItemsRepository bookingItemsRepository;
    RoomRepository roomRepository;
    PaymentBillRepository paymentBillRepository;
    HotelOffersRepository hotelOffersRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var user = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        Booking booking = Booking.builder()
                .bookingDate(LocalDate.now())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .user(user)
                .bookingRooms(new ArrayList<>())
                .bookingItems(new ArrayList<>())
                .totalRoomPrice(0L)
                .totalServicePrice(0L)
                .grandTotal(0L)
                .build();

        if (request.getBookingRoomIds() != null && !request.getBookingRoomIds().isEmpty()) {
            List<BookingRoom> rooms = bookingRoomRepository.findByUsernameAndBookingRoomIdIn(username, request.getBookingRoomIds());
            for (BookingRoom br : rooms) {
                br.setBooking(booking);
                br.setBookingStatus(BookingStatus.IN_PROGRESS);
                booking.getBookingRooms().add(br);
            }
        }

        if (request.getBookingItemIds() != null && !request.getBookingItemIds().isEmpty()) {
            List<BookingItems> items = bookingItemsRepository.findByUsernameAndBookingItemIdIn(username, request.getBookingItemIds());
            for (BookingItems item : items) {
                item.setBooking(booking);
                booking.getBookingItems().add(item);
            }
        }

        booking.calculateTotals();
        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    @Transactional
    public void processSuccessfulPayment(String bookingId, String transactionId, Long amount, PaymentMethod method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentDate(LocalDateTime.now());
        booking.setPaymentTransactionId(transactionId);

        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CONFIRMED));
        }

        bookingRepository.save(booking);

        PaymentBill paymentLog = PaymentBill.builder()
                .transactionId(transactionId)
                .booking(booking)
                .user(booking.getUser())
                .paidAmount(amount)
                .paymentMethod(method)
                .status(PaymentStatus.COMPLETED)
                .createAt(LocalDateTime.now())
                .build();

        paymentBillRepository.save(paymentLog);
    }

    @Transactional
    public BookingResponse addServiceToBooking(String bookingId, String offerId, int quantity) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        HotelOffers offer = hotelOffersRepository.findById(offerId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        BookingItems item = BookingItems.builder()
                .hotelOffers(offer)
                .quantity(quantity)
                .totalItemsPrice(offer.getPrice() * quantity)
                .username(booking.getUser().getUsername())
                .booking(booking)
                .build();

        booking.addBookingItem(item);
        booking.calculateTotals();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToBookingResponse(savedBooking);
    }

    public Page<BookingResponse> getAllBookingByUserId(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findByUser_Id(customerId, pageable);
        if (result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return result.map(this::mapToBookingResponse);
    }

    public BookingResponse getBookingById(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public void deleteBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (booking.getBookingRooms() != null) {
            for (BookingRoom bookingRoom : booking.getBookingRooms()) {
                List<Room> rooms = bookingRoom.getRooms();
                if (rooms != null) {
                    rooms.forEach(room -> {
                        room.setRoomStatus(RoomStatus.AVAILABLE);
                        room.setBookingRoom(null);
                    });
                    roomRepository.saveAll(rooms);
                }
            }
        }
        bookingRepository.delete(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingRoomResponse> roomRes = booking.getBookingRooms().stream()
                .map(br -> BookingRoomResponse.builder()
                        .bookingRoomId(br.getBookingRoomId())
                        .checkInDate(br.getCheckInDate())
                        .checkOutDate(br.getCheckOutDate())
                        .totalRoomAmount(br.getTotalRoomAmount())
                        .rooms(br.getRooms().stream().map(Room::getRoomNumber).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<BookingItemResponse> itemRes = booking.getBookingItems().stream()
                .map(bi -> BookingItemResponse.builder()
                        .bookingItemId(bi.getBookingItemId())
                        // FIX TẠI ĐÂY: Đổi hotelOffer -> hotelOfferName và thêm map data
                        .hotelOfferName(bi.getHotelOffers().getName())
                        .imageUrl(bi.getHotelOffers().getImageUrl())
                        .unitPrice(bi.getHotelOffers().getPrice())
                        .quantity(bi.getQuantity())
                        .totalItemsPrice(bi.getTotalItemsPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingDate(booking.getBookingDate())
                .bookingStatus(booking.getBookingStatus().toString())
                .paymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().toString() : "N/A")
                .totalRoomPrice(booking.getTotalRoomPrice())
                .totalBookingServicePrice(booking.getTotalServicePrice())
                .grandTotal(booking.getGrandTotal())
                .customerName(booking.getUser().getName())
                .bookingRooms(roomRes)
                .bookingItems(itemRes)
                .build();
    }
}