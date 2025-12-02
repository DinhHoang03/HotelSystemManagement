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
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.SuccessfulPaymentResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.user_service.models.repositories.UserRepository;
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

    // --- 1. TẠO BOOKING ---
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        User user = userRepository.findByUsername(username)
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
                .surcharge(0L)
                .build();

        Booking savedBooking = bookingRepository.saveAndFlush(booking);

        // Link Booking Rooms
        if (request.getBookingRoomIds() != null && !request.getBookingRoomIds().isEmpty()) {
            List<BookingRoom> rooms = bookingRoomRepository.findByUsernameAndBookingRoomIdIn(username, request.getBookingRoomIds());
            if (rooms.size() != request.getBookingRoomIds().size()) throw new AppException(AppErrorCode.INVALID_BOOKING_DATA);

            for (BookingRoom br : rooms) {
                br.setBooking(savedBooking);
                br.setBookingStatus(BookingStatus.PENDING);
            }
            savedBooking.getBookingRooms().addAll(bookingRoomRepository.saveAll(rooms));
        }

        // Link Booking Items
        if (request.getBookingItemIds() != null && !request.getBookingItemIds().isEmpty()) {
            List<BookingItems> items = bookingItemsRepository.findByUsernameAndBookingItemIdIn(username, request.getBookingItemIds());
            if (items.size() != request.getBookingItemIds().size()) throw new AppException(AppErrorCode.INVALID_BOOKING_DATA);

            for (BookingItems bi : items) {
                bi.setBooking(savedBooking);
                bi.setBookingStatus(BookingStatus.PENDING);
            }
            savedBooking.getBookingItems().addAll(bookingItemsRepository.saveAll(items));
        }

        updateBookingTotals(savedBooking);
        return mapToBookingResponse(bookingRepository.save(savedBooking));
    }

    // --- 2. LẤY DANH SÁCH BOOKING (Đã Fix lỗi 400) ---
    public Page<BookingResponse> getAllBookingByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // FIX: Dùng findByUser (Object) thay vì findByUser_Id
        Page<Booking> result = bookingRepository.findByUser(user, pageable);

        if (result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return result.map(this::mapToBookingResponse);
    }

    // --- 3. CHI TIẾT BOOKING ---
    public BookingResponse getBookingById(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }
        return mapToBookingResponse(booking);
    }

    // --- 4. HỦY BOOKING (Soft Delete - Trả phòng) ---
    @Transactional
    public BookingResponse cancelBooking(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (!booking.getUser().getUsername().equals(username)) {
            // Nếu là Admin muốn hủy giùm thì có thể thêm logic check role tại đây
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }

        if (booking.getBookingStatus() == BookingStatus.CHECKED_IN || booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new RuntimeException("Cannot cancel booking that is already checked-in/out");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Hủy phòng và NHẢ PHÒNG TRỐNG (AVAILABLE)
        if (booking.getBookingRooms() != null) {
            for (BookingRoom br : booking.getBookingRooms()) {
                br.setBookingStatus(BookingStatus.CANCELLED);
                if (br.getRooms() != null) {
                    br.getRooms().forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
                    roomRepository.saveAll(br.getRooms());
                }
            }
        }

        // Hủy dịch vụ
        if (booking.getBookingItems() != null) {
            booking.getBookingItems().forEach(bi -> bi.setBookingStatus(BookingStatus.CANCELLED));
        }

        return mapToBookingResponse(bookingRepository.save(booking));
    }

    // --- 5. KHÔI PHỤC BOOKING (Restore - Check phòng trống) ---
    @Transactional
    public BookingResponse restoreBooking(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (!booking.getUser().getUsername().equals(username)) throw new AppException(AppErrorCode.UNAUTHORIZED);

        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new RuntimeException("Only CANCELLED bookings can be restored.");
        }

        // Check xem các phòng cũ còn trống không
        if (booking.getBookingRooms() != null) {
            for (BookingRoom br : booking.getBookingRooms()) {
                if (br.getRooms() != null) {
                    for (Room room : br.getRooms()) {
                        if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
                            throw new RuntimeException("Room " + room.getRoomNumber() + " is already taken. Cannot restore.");
                        }
                        room.setRoomStatus(RoomStatus.BOOKED);
                    }
                    roomRepository.saveAll(br.getRooms());
                }
                br.setBookingStatus(BookingStatus.PENDING);
            }
        }

        if (booking.getBookingItems() != null) {
            booking.getBookingItems().forEach(bi -> bi.setBookingStatus(BookingStatus.PENDING));
        }

        booking.setBookingStatus(BookingStatus.PENDING);
        return mapToBookingResponse(bookingRepository.save(booking));
    }

    // --- 6. XÓA VĨNH VIỄN (Hard Delete) ---
    @Transactional
    public void deleteBookingPermanently(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (!booking.getUser().getUsername().equals(username)) throw new AppException(AppErrorCode.UNAUTHORIZED);

        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new RuntimeException("Only CANCELLED bookings can be deleted permanently.");
        }
        bookingRepository.delete(booking);
    }

    // --- 7. LẤY DANH SÁCH ĐÃ HỦY ---
    public Page<BookingResponse> getCancelledBookings(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        Page<Booking> cancelledPage = bookingRepository.findByUserAndBookingStatus(
                user,
                BookingStatus.CANCELLED,
                pageable
        );

        if (cancelledPage.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return cancelledPage.map(this::mapToBookingResponse);
    }

    // --- 8. THANH TOÁN THÀNH CÔNG (Callback từ cổng thanh toán) ---
    @Transactional
    public SuccessfulPaymentResponse processSuccessfulPayment(String bookingId, String transactionId, Long amount, PaymentMethod method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentDate(LocalDateTime.now());
        booking.setPaymentTransactionId(transactionId);

        if (booking.getBookingRooms() != null) booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CONFIRMED));
        if (booking.getBookingItems() != null) booking.getBookingItems().forEach(bi -> bi.setBookingStatus(BookingStatus.CONFIRMED));

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
        var result = paymentBillRepository.save(paymentLog);

        return SuccessfulPaymentResponse.builder()
                .booking(result.getBooking())
                .status(result.getStatus())
                .build();
    }

    // --- 9. CHECK-IN & CHECK-OUT ---
    @Transactional
    public BookingResponse checkIn(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CONFIRMED before Check-in");
        }
        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        if (booking.getBookingRooms() != null) booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CHECKED_IN));

        return mapToBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse checkOut(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Invalid checkout status");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plannedCheckOut = now;
        if (!booking.getBookingRooms().isEmpty()) {
            plannedCheckOut = booking.getBookingRooms().get(0).getCheckOutDate().atTime(12, 0);
        }

        long surcharge = 0;
        if (now.isAfter(plannedCheckOut)) {
            long hoursLate = java.time.Duration.between(plannedCheckOut, now).toHours();
            if (hoursLate > 0) surcharge = hoursLate * 50000;
        }

        booking.setActualCheckOutDate(now);
        booking.setSurcharge(surcharge);
        booking.setBookingStatus(BookingStatus.CHECKED_OUT);

        if (booking.getBookingRooms() != null) booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CHECKED_OUT));
        if (surcharge > 0) booking.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);

        updateBookingTotals(booking);

        // Nhả phòng
        if (booking.getBookingRooms() != null) {
            for (BookingRoom br : booking.getBookingRooms()) {
                if (br.getRooms() != null) {
                    br.getRooms().forEach(room -> {
                        room.setRoomStatus(RoomStatus.AVAILABLE);
                        room.setClean(false); // Cần dọn dẹp
                    });
                    roomRepository.saveAll(br.getRooms());
                }
            }
        }
        return mapToBookingResponse(bookingRepository.save(booking));
    }

    // --- HELPER METHODS ---
    @Transactional
    public BookingResponse addServiceToBooking(String bookingId, String offerId, int quantity, String username) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (!booking.getUser().getUsername().equals(username)) throw new AppException(AppErrorCode.UNAUTHORIZED);
        HotelOffers offer = hotelOffersRepository.findById(offerId).orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        BookingItems item = BookingItems.builder()
                .hotelOffers(offer)
                .quantity(quantity)
                .totalItemsPrice(offer.getPrice() * quantity)
                .username(username)
                .booking(booking)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        if (booking.getBookingItems() == null) booking.setBookingItems(new ArrayList<>());
        booking.getBookingItems().add(item);
        bookingItemsRepository.save(item);

        updateBookingTotals(booking);
        return mapToBookingResponse(bookingRepository.save(booking));
    }

    private void updateBookingTotals(Booking booking) {
        long totalRoom = (booking.getBookingRooms() != null) ? booking.getBookingRooms().stream().mapToLong(BookingRoom::getTotalRoomAmount).sum() : 0L;
        long totalService = (booking.getBookingItems() != null) ? booking.getBookingItems().stream().mapToLong(BookingItems::getTotalItemsPrice).sum() : 0L;
        long surcharge = booking.getSurcharge() == null ? 0L : booking.getSurcharge();

        booking.setTotalRoomPrice(totalRoom);
        booking.setTotalServicePrice(totalService);
        booking.setGrandTotal(totalRoom + totalService + surcharge);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingRoomResponse> roomRes = new ArrayList<>();
        if (booking.getBookingRooms() != null) {
            roomRes = booking.getBookingRooms().stream().map(br -> BookingRoomResponse.builder()
                    .bookingRoomId(br.getBookingRoomId())
                    .checkInDate(br.getCheckInDate())
                    .checkOutDate(br.getCheckOutDate())
                    .totalRoomAmount(br.getTotalRoomAmount())
                    .rooms(br.getRooms() == null ? new ArrayList<>() : br.getRooms().stream().map(this::mapRoomToResponse).collect(Collectors.toList()))
                    .build()).collect(Collectors.toList());
        }
        List<BookingItemResponse> itemRes = new ArrayList<>();
        if (booking.getBookingItems() != null) {
            itemRes = booking.getBookingItems().stream().map(bi -> BookingItemResponse.builder()
                    .bookingItemId(bi.getBookingItemId())
                    .hotelOfferName(bi.getHotelOffers().getName())
                    .imageUrl(bi.getHotelOffers().getImageUrl())
                    .unitPrice(bi.getHotelOffers().getPrice())
                    .quantity(bi.getQuantity())
                    .totalItemsPrice(bi.getTotalItemsPrice())
                    .build()).collect(Collectors.toList());
        }
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingDate(booking.getBookingDate())
                .bookingStatus(booking.getBookingStatus().toString())
                .paymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().toString() : "N/A")
                .totalRoomPrice(booking.getTotalRoomPrice())
                .totalBookingServicePrice(booking.getTotalServicePrice())
                .grandTotal(booking.getGrandTotal())
                .customerName(booking.getUser() != null ? booking.getUser().getName() : "Unknown")
                .bookingRooms(roomRes)
                .bookingItems(itemRes)
                .build();
    }

    private RoomResponse mapRoomToResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomStatus(room.getRoomStatus() != null ? room.getRoomStatus().name() : null)
                .floor(room.getFloor())
                .viewType(room.getViewType())
                .isClean(room.isClean())
                .roomTypeId(room.getRoomType() != null ? room.getRoomType().getRoomTypeId() : null)
                .roomTypeName(room.getRoomType() != null ? room.getRoomType().getRoomTypes() : null)
                .priceByDay(room.getRoomType() != null ? room.getRoomType().getFullDayPrice() : null)
                .maxAdults(room.getRoomType() != null ? room.getRoomType().getMaxAdults() : null)
                .imageUrl(room.getRoomType() != null ? room.getRoomType().getImageUrl() : null)
                .build();
    }
}