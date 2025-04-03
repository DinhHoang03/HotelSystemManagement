package com.humg.HotelSystemManagement.service.SystemServices.booking;

import com.humg.HotelSystemManagement.dto.request.bookingRoom.BookingRoomRequest;
import com.humg.HotelSystemManagement.dto.response.bookingRoom.BookingRoomResponse;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.entity.enums.RoomStatus;
import com.humg.HotelSystemManagement.entity.roomManagerment.Room;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
import com.humg.HotelSystemManagement.service.ISimpleCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingRoomService implements ISimpleCRUDService<BookingRoomResponse, BookingRoomRequest, String> {
    BookingRoomRepository bookingRoomRepository;
    RoomRepository roomRepository;

    @Override
    public BookingRoomResponse create(BookingRoomRequest request) {

        if(request == null) {
            throw new AppException(AppErrorCode.REQUEST_IS_NULL);
        }

        var listRoom = roomRepository.findAllByRoomNumberIn(request.getRoomNumbers());

        if(listRoom.isEmpty() || listRoom.size() != request.getRoomNumbers().size()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        List<String> unavailableRooms = listRoom.stream()
                .filter(room -> room.getRoomStatus() != RoomStatus.AVAILABLE)
                .map(Room::getRoomNumber)
                .collect(Collectors.toList());

        if(!unavailableRooms.isEmpty()) {
            System.out.println("Room that not available: " + String.join(", ", unavailableRooms));
            throw new AppException(AppErrorCode.ROOM_NOT_AVAILABLE);
        }

        var checkInDate = request.getCheckInDate();
        var checkOutDate = request.getCheckOutDate();

        if(checkInDate.isAfter(checkOutDate)) {
            throw new AppException(AppErrorCode.INVALID_DATE);
        }

        List<BookingRoom> conflictingBookings = bookingRoomRepository
                .findBookedRoomNumbersInDateRangeForRooms
                        (
                                listRoom.stream()
                                        .map(Room::getRoomNumber)
                                        .toList(),
                                checkInDate,
                                checkOutDate
                        );

        if(!conflictingBookings.isEmpty()) {
            List<String> bookedRoomNumbers = conflictingBookings.stream()
                    .flatMap(
                            br -> br.getRooms()
                                    .stream()
                                    .map(Room::getRoomNumber))
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("Room already booked: " + String.join(", ", bookedRoomNumbers));
            throw new AppException(AppErrorCode.ROOM_ALREADY_BOOKED);
        }

        var totalRoomAmount = totalRoomAmount(listRoom, checkInDate, checkOutDate);

        BookingRoom bookingRoom = BookingRoom.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .totalRoomAmount(totalRoomAmount)
                .bookingStatus(BookingStatus.PENDING)
                .rooms(listRoom)
                .build();

        var result = bookingRoomRepository.save(bookingRoom);

        return BookingRoomResponse.builder()
                .bookingRoomId(result.getBookingRoomId())
                .checkInDate(result.getCheckInDate())
                .checkOutDate(result.getCheckOutDate())
                .totalRoomAmount(result.getTotalRoomAmount())
                .bookingStatus(result.getBookingStatus().toString())
                .rooms(listRoom)
                .build();
    }

    public Long totalRoomAmount(List<Room> rooms, LocalDate checkInDate, LocalDate checkOutDate){
        if(rooms == null || rooms.isEmpty() || checkInDate == null || checkOutDate == null) {
            return 0L;
        }

        LocalDateTime start = checkInDate.atStartOfDay(); //00:00:00
        LocalDateTime end = checkOutDate.atStartOfDay(); //00:00:00

        long numberOfHours = ChronoUnit.HOURS.between(start, end);
        if(numberOfHours <= 0) {
            throw new AppException(AppErrorCode.INVALID_DATE);
        }

        double numberOfHalfDays = (double) numberOfHours / 12; //Chia nửa ngày
        long roundedHalfDays = Math.round(numberOfHalfDays);

        if(roundedHalfDays == 1) { //Nhỏ hơn 1 ngày
            return rooms.stream()
                    .map(room -> room.getRoomType().getHalfDayPrice())
                    .reduce(0L, Long::sum);
        } else if (roundedHalfDays <= 13) { //Nhỏ hơn 6.5 ngày
            long numberOfFullDays = roundedHalfDays / 2;
            return rooms.stream()
                    .map(room -> room.getRoomType().getFullDayPrice() * numberOfFullDays)
                    .reduce(0L, Long::sum);
        }else {
            long numberOfWeeks = (roundedHalfDays + 13) / 14;
            return rooms.stream()
                    .map(room -> room.getRoomType().getFullWeekPrice() * numberOfWeeks)
                    .reduce(0L, Long::sum);
        }
    }

    @Override
    public Page<BookingRoomResponse> getAll(int page, int size) {
        return null;
    }

    @Override
    public BookingRoomResponse getById(String id) {
        return null;
    }

    @Override
    public BookingRoomResponse update(String id, BookingRoomRequest request) {
        return null;
    }

    @Override
    public void delete(String id) {
        var booking = bookingRoomRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        booking.getRooms().forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
        roomRepository.saveAll(booking.getRooms());

        bookingRoomRepository.delete(booking);
    }
}
