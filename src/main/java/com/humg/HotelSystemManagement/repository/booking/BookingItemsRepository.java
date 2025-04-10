package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.BookingItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingItemsRepository extends JpaRepository<BookingItems, String> {
    List<BookingItems> findByUsernameAndBookingItemIdIn(String username, List<String> bookingItemIds);
    //void deleteByBookingIsNullAndCreatedDateBefore(LocalDateTime threshold); // Để xóa dữ liệu mồ côi
}
