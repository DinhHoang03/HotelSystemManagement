package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {
}
