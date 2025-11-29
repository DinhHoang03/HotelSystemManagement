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
import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.customer_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories.HotelOffersRepository;
import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
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
        // 1. Kiểm tra request và User
        if (request == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // Tạo Booking mới (Khởi tạo list rỗng ngay từ đầu để tránh Null)
        Booking booking = Booking.builder()
                .bookingDate(LocalDate.now())
                .bookingStatus(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .user(user)
                .bookingRooms(new ArrayList<>()) // Khởi tạo list rỗng
                .bookingItems(new ArrayList<>()) // Khởi tạo list rỗng
                .totalRoomPrice(0L)
                .totalServicePrice(0L)
                .grandTotal(0L)
                .surcharge(0L)
                .build();

        // Lưu Booking cha trước
        Booking savedBooking = bookingRepository.saveAndFlush(booking);

        // 2. Xử lý Booking Rooms (Cập nhật từ IN_CART -> PENDING và gán vào Booking)
        if (request.getBookingRoomIds() != null && !request.getBookingRoomIds().isEmpty()) {
            List<BookingRoom> rooms = bookingRoomRepository.findByUsernameAndBookingRoomIdIn(username, request.getBookingRoomIds());

            if (rooms.size() != request.getBookingRoomIds().size()) {
                throw new AppException(AppErrorCode.INVALID_BOOKING_DATA);
            }

            // Cập nhật quan hệ và trạng thái
            for (BookingRoom br : rooms) {
                br.setBooking(savedBooking);
                br.setBookingStatus(BookingStatus.PENDING);
            }
            List<BookingRoom> savedRooms = bookingRoomRepository.saveAll(rooms);

            // Cập nhật lại list trong bộ nhớ của savedBooking (KHÔNG DÙNG clear())
            if (savedBooking.getBookingRooms() == null) {
                savedBooking.setBookingRooms(new ArrayList<>());
            }
            savedBooking.getBookingRooms().addAll(savedRooms);
        }

        // 3. Xử lý Booking Items (Cập nhật từ IN_CART -> PENDING và gán vào Booking)
        if (request.getBookingItemIds() != null && !request.getBookingItemIds().isEmpty()) {
            List<BookingItems> items = bookingItemsRepository.findByUsernameAndBookingItemIdIn(username, request.getBookingItemIds());

            if (items.size() != request.getBookingItemIds().size()) {
                throw new AppException(AppErrorCode.INVALID_BOOKING_DATA);
            }

            // Cập nhật quan hệ và trạng thái
            for (BookingItems bi : items) {
                bi.setBooking(savedBooking);
                bi.setBookingStatus(BookingStatus.PENDING);
            }
            List<BookingItems> savedItems = bookingItemsRepository.saveAll(items);

            // Cập nhật lại list trong bộ nhớ của savedBooking (KHÔNG DÙNG clear())
            if (savedBooking.getBookingItems() == null) {
                savedBooking.setBookingItems(new ArrayList<>());
            }
            savedBooking.getBookingItems().addAll(savedItems);
        }

        // 4. Tính toán lại tổng tiền sau khi đã gán phòng và dịch vụ
        updateBookingTotals(savedBooking);

        // Lưu lại lần cuối để cập nhật giá tiền vào DB và trả về response
        return mapToBookingResponse(bookingRepository.save(savedBooking));
    }

    // =================================================================
    // DƯỚI ĐÂY LÀ PHẦN COPY LẠI TỪ CODE CŨ (ĐỂ ĐẦY ĐỦ FILE)
    // =================================================================

    public BookingResponse getBookingById(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }
        return mapToBookingResponse(booking);
    }

    @Transactional
    public void deleteBooking(String bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }
        if (booking.getBookingRooms() != null) {
            for (BookingRoom br : booking.getBookingRooms()) {
                if (br.getRooms() != null) {
                    br.getRooms().forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
                    roomRepository.saveAll(br.getRooms());
                }
            }
        }
        bookingRepository.delete(booking);
    }

    @Transactional
    public BookingResponse addServiceToBooking(String bookingId, String offerId, int quantity, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }
        HotelOffers offer = hotelOffersRepository.findById(offerId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        BookingItems item = BookingItems.builder()
                .hotelOffers(offer)
                .quantity(quantity)
                .totalItemsPrice(offer.getPrice() * quantity)
                .username(username)
                .booking(booking)
                .bookingStatus(BookingStatus.PENDING) // Thêm trực tiếp vào đơn thì là PENDING luôn
                .build();
        if (booking.getBookingItems() == null) booking.setBookingItems(new ArrayList<>());
        booking.getBookingItems().add(item);
        bookingItemsRepository.save(item);
        updateBookingTotals(booking);
        return mapToBookingResponse(bookingRepository.save(booking));
    }

    public Page<BookingResponse> getAllBookingByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));
        Page<Booking> result = bookingRepository.findByUser_Id(user.getId(), pageable);
        if (result.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);
        return result.map(this::mapToBookingResponse);
    }

    @Transactional
    public SuccessfulPaymentResponse processSuccessfulPayment(String bookingId, String transactionId, Long amount, PaymentMethod method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentDate(LocalDateTime.now());
        booking.setPaymentTransactionId(transactionId);

        // Update con
        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CONFIRMED));
        }
        if (booking.getBookingItems() != null) {
            booking.getBookingItems().forEach(bi -> bi.setBookingStatus(BookingStatus.CONFIRMED));
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
        var result = paymentBillRepository.save(paymentLog);

        return SuccessfulPaymentResponse.builder()
                .booking(result.getBooking())
                .status(result.getStatus())
                .build();
    }

    @Transactional
    public BookingResponse checkOut(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ được checkout khi khách đang ở hoặc đã xác nhận");
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

        // Update con sang check out
        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CHECKED_OUT));
        }

        if (surcharge > 0) booking.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        updateBookingTotals(booking);
        if (booking.getBookingRooms() != null) {
            for (BookingRoom br : booking.getBookingRooms()) {
                if (br.getRooms() != null) {
                    br.getRooms().forEach(room -> {
                        room.setRoomStatus(RoomStatus.AVAILABLE);
                        room.setClean(false);
                    });
                    roomRepository.saveAll(br.getRooms());
                }
            }
        }
        return mapToBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse checkIn(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking phải được xác nhận trước khi Check-in");
        }
        booking.setBookingStatus(BookingStatus.CHECKED_IN);

        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().forEach(br -> br.setBookingStatus(BookingStatus.CHECKED_IN));
        }

        return mapToBookingResponse(bookingRepository.save(booking));
    }

    private void updateBookingTotals(Booking booking) {
        long totalRoom = 0L;
        long totalService = 0L;
        if (booking.getBookingRooms() != null) {
            totalRoom = booking.getBookingRooms().stream().mapToLong(BookingRoom::getTotalRoomAmount).sum();
        }
        if (booking.getBookingItems() != null) {
            totalService = booking.getBookingItems().stream().mapToLong(BookingItems::getTotalItemsPrice).sum();
        }
        booking.setTotalRoomPrice(totalRoom);
        booking.setTotalServicePrice(totalService);
        long surcharge = booking.getSurcharge() == null ? 0L : booking.getSurcharge();
        booking.setGrandTotal(totalRoom + totalService + surcharge);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingRoomResponse> roomRes = new ArrayList<>();
        if (booking.getBookingRooms() != null) {
            roomRes = booking.getBookingRooms().stream().map(br -> BookingRoomResponse.builder().bookingRoomId(br.getBookingRoomId()).checkInDate(br.getCheckInDate()).checkOutDate(br.getCheckOutDate()).totalRoomAmount(br.getTotalRoomAmount()).rooms(br.getRooms() == null ? new ArrayList<>() : br.getRooms().stream().map(this::mapRoomToResponse).collect(Collectors.toList())).build()).collect(Collectors.toList());
        }
        List<BookingItemResponse> itemRes = new ArrayList<>();
        if (booking.getBookingItems() != null) {
            itemRes = booking.getBookingItems().stream().map(bi -> BookingItemResponse.builder().bookingItemId(bi.getBookingItemId()).hotelOfferName(bi.getHotelOffers().getName()).imageUrl(bi.getHotelOffers().getImageUrl()).unitPrice(bi.getHotelOffers().getPrice()).quantity(bi.getQuantity()).totalItemsPrice(bi.getTotalItemsPrice()).build()).collect(Collectors.toList());
        }
        return BookingResponse.builder().bookingId(booking.getBookingId()).bookingDate(booking.getBookingDate()).bookingStatus(booking.getBookingStatus().toString()).paymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().toString() : "N/A").totalRoomPrice(booking.getTotalRoomPrice()).totalBookingServicePrice(booking.getTotalServicePrice()).grandTotal(booking.getGrandTotal()).customerName(booking.getUser() != null ? booking.getUser().getName() : "Unknown").bookingRooms(roomRes).bookingItems(itemRes).build();
    }

    private RoomResponse mapRoomToResponse(Room room) {
        return RoomResponse.builder().roomId(room.getRoomId()).roomNumber(room.getRoomNumber()).roomStatus(room.getRoomStatus() != null ? room.getRoomStatus().name() : null).floor(room.getFloor()).viewType(room.getViewType()).isClean(room.isClean()).roomTypeId(room.getRoomType() != null ? room.getRoomType().getRoomTypeId() : null).roomTypeName(room.getRoomType() != null ? room.getRoomType().getRoomTypes() : null).priceByDay(room.getRoomType() != null ? room.getRoomType().getFullDayPrice() : null).maxAdults(room.getRoomType() != null ? room.getRoomType().getMaxAdults() : null).imageUrl(room.getRoomType() != null ? room.getRoomType().getImageUrl() : null).build();
    }
}