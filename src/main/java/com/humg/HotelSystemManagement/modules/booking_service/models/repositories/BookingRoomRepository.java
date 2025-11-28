package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, String> {

    // Query check phòng trống giữ nguyên
    @Query("SELECT DISTINCT r.roomNumber FROM BookingRoom br JOIN br.rooms r " +
            "WHERE r.roomNumber IN :roomNumbers " +
            "AND (br.checkInDate <= :checkOutDate AND br.checkOutDate >= :checkInDate) " +
            "AND br.bookingStatus <> 'CANCELLED'") // Nên thêm điều kiện này để không check các đơn đã hủy
    List<String> findBookedRoomNumbersInDateRangeForRooms(@Param("roomNumbers") List<String> roomNumbers,
                                                          @Param("checkInDate") LocalDate checkInDate,
                                                          @Param("checkOutDate") LocalDate checkOutDate);

    // Nếu BookingRoom có trường "username" (String) thì giữ nguyên
    List<BookingRoom> findByUsernameAndBookingRoomIdIn(String username, List<String> bookingRoomIds);

    Page<BookingRoom> findByUsername(String username, Pageable pageable);

    List<BookingRoom> findByBooking(Booking booking);

    @Query("SELECT br FROM BookingRoom br WHERE br.checkInDate <= :date AND br.checkOutDate >= :date " +
            "AND br.bookingStatus = 'CONFIRMED'")
    List<BookingRoom> findActiveBookingsOnDate(LocalDate date);
}