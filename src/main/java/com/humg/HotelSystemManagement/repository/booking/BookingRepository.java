package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

}
