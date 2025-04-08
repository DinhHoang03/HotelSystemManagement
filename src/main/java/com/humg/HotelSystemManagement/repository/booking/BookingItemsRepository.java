package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingItemsRepository extends JpaRepository<BookingItems, String> {
}
