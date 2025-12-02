package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemsRepository extends JpaRepository<BookingItems, String> {

    // API Mới: Lấy danh sách món ăn ĐANG ĐI CHỢ (Chưa gắn vào Booking nào)
    Page<BookingItems> findByUsernameAndBookingStatus(String username, BookingStatus status, Pageable pageable);

    // Hỗ trợ Security check
    List<BookingItems> findByUsernameAndBookingItemIdIn(String username, List<String> ids);

    @Query("SELECT ho.name, SUM(bi.quantity), SUM(bi.totalItemsPrice) " +
            "FROM BookingItems bi " +
            "JOIN bi.hotelOffers ho " +
            "WHERE bi.bookingStatus = 'BOOKED' " + // Hoặc check booking cha đã COMPLETED
            "GROUP BY ho.name " +
            "ORDER BY SUM(bi.quantity) DESC " +
            "LIMIT 5")
    List<Object[]> findTopServices();
}