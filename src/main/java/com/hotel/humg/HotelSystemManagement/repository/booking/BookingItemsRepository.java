package com.hotel.humg.HotelSystemManagement.repository.booking;

import com.hotel.humg.HotelSystemManagement.entity.booking.BookingItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemsRepository extends JpaRepository<BookingItems, String> {
    List<BookingItems> findByUsernameAndBookingItemIdIn(String username, List<String> bookingItemIds);
    int deleteByBookingIsNull(); // Để xóa dữ liệu mồ côi

}
