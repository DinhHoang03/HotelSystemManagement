package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, String> {
    @Query("SELECT DISTINCT r.roomNumber FROM BookingRoom br JOIN br.rooms r " +
            "WHERE r.roomNumber IN :roomNumbers " +
            "AND (br.checkInDate <= :checkOutDate AND br.checkOutDate >= :checkInDate)")
    List<BookingRoom> findBookedRoomNumbersInDateRangeForRooms(@Param("roomNumbers") List<String> roomNumbers,
                                                          @Param("checkInDate") LocalDate checkInDate,
                                                          @Param("checkOutDate") LocalDate checkOutDate);

    List<BookingRoom> findByUsernameAndBookingRoomIdIn(String username, List<String> bookingItemsId);
    int deleteByBookingIsNull();
}