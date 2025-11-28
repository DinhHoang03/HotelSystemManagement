package com.humg.HotelSystemManagement.modules.booking_service.services;

import com.humg.HotelSystemManagement.modules.booking_service.resources.requests.BookingRoomRequest;
import com.humg.HotelSystemManagement.modules.booking_service.resources.responses.BookingRoomResponse;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRoomRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.utils.interfaces.ISimpleCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingRoomService implements ISimpleCRUDService<BookingRoomResponse, BookingRoomRequest, String> {

    BookingRoomRepository bookingRoomRepository;
    RoomRepository roomRepository;

    @Transactional
    public BookingRoomResponse createOrder(BookingRoomRequest request, String username) {
        if (request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        LocalDate checkInDate = request.getCheckInDate();
        LocalDate checkOutDate = request.getCheckOutDate();

        if (checkInDate == null || checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            throw new AppException(AppErrorCode.INVALID_DATE);
        }

        Set<String> distinctRoomNumbers = new HashSet<>(request.getRoomNumbers());
        List<Room> listRoom = roomRepository.findAllByRoomNumberIn(distinctRoomNumbers.stream().toList());

        if (listRoom.isEmpty() || listRoom.size() != distinctRoomNumbers.size()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        // --- SỬA Ở ĐÂY: Đổi List<BookingRoom> thành List<String> ---
        List<String> conflictingRoomNumbers = bookingRoomRepository
                .findBookedRoomNumbersInDateRangeForRooms(
                        listRoom.stream().map(Room::getRoomNumber).toList(),
                        checkInDate,
                        checkOutDate
                );

        if (!conflictingRoomNumbers.isEmpty()) {
            // Có thể log ra số phòng bị trùng để debug dễ hơn: conflictingRoomNumbers.toString()
            throw new AppException(AppErrorCode.ROOM_ALREADY_BOOKED);
        }

        long totalRoomAmount = calculateTotalAmount(listRoom, checkInDate, checkOutDate);

        // Tạo Entity
        BookingRoom bookingRoom = BookingRoom.builder()
                .username(username)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .totalRoomAmount(totalRoomAmount)
                .bookingStatus(BookingStatus.PENDING)
                .rooms(listRoom) // Hibernate tự động lưu vào bảng trung gian booking_room_detail
                .build();

        var savedBooking = bookingRoomRepository.save(bookingRoom);

        // --- ĐOẠN SỬA QUAN TRỌNG NHẤT ---
        // Thay vì gọi mapToResponse(savedBooking) (có thể bị rỗng list rooms do Lazy Load sau khi save)
        // Ta build response thủ công dùng ngay cái listRoom ta đang có trong tay.
        return BookingRoomResponse.builder()
                .bookingRoomId(savedBooking.getBookingRoomId())
                .checkInDate(savedBooking.getCheckInDate())
                .checkOutDate(savedBooking.getCheckOutDate())
                .totalRoomAmount(savedBooking.getTotalRoomAmount())
                .rooms(listRoom.stream().map(this::mapRoomToResponse).collect(Collectors.toList()))
                .build();
    }

    private Long calculateTotalAmount(List<Room> rooms, LocalDate checkInDate, LocalDate checkOutDate) {
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) numberOfNights = 1;

        final long nights = numberOfNights;
        return rooms.stream()
                .map(room -> room.getRoomType().getFullDayPrice() * nights)
                .reduce(0L, Long::sum);
    }

    @Override
    @Transactional(readOnly = true) // Đã thêm Transactional để fix lỗi Lazy Loading khi Get
    public Page<BookingRoomResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRoomRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true) // Đã thêm Transactional
    public Page<BookingRoomResponse> getAllByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRoomRepository.findByUsername(username, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true) // Đã thêm Transactional
    public BookingRoomResponse getById(String id) {
        return bookingRoomRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!bookingRoomRepository.existsById(id)) {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }
        bookingRoomRepository.deleteById(id);
    }

    @Override
    public BookingRoomResponse create(BookingRoomRequest request) { return null; }

    @Override
    public BookingRoomResponse update(String id, BookingRoomRequest request) { return null; }

    // --- MAPPER ---

    private BookingRoomResponse mapToResponse(BookingRoom entity) {
        return BookingRoomResponse.builder()
                .bookingRoomId(entity.getBookingRoomId())
                .checkInDate(entity.getCheckInDate())
                .checkOutDate(entity.getCheckOutDate())
                .totalRoomAmount(entity.getTotalRoomAmount())
                // Lưu ý: Nếu entity được lấy từ DB mà session đã đóng, dòng này có thể gây lỗi Lazy
                // Nhưng nhờ @Transactional(readOnly=true) ở các hàm get, nó sẽ hoạt động tốt.
                .rooms(entity.getRooms().stream()
                        .map(this::mapRoomToResponse)
                        .collect(Collectors.toList()))
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
                .imageUrl(room.getRoomType() != null ? room.getRoomType().getImageUrl() : null)
                .roomTypeId(room.getRoomType() != null ? room.getRoomType().getRoomTypeId() : null)
                .roomTypeName(room.getRoomType() != null ? room.getRoomType().getRoomTypes() : null)
                .priceByDay(room.getRoomType() != null ? room.getRoomType().getFullDayPrice() : null)
                .maxAdults(room.getRoomType() != null ? room.getRoomType().getMaxAdults() : null)
                .build();
    }
}