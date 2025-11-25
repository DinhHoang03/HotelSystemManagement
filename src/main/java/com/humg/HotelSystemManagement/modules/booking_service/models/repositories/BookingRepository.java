package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    // SỬA: Đổi Customer thành User
    Page<Booking> findByUser_Id(String userId, Pageable pageable);

    // Query thống kê này không dính đến Customer nên giữ nguyên
    @Query("SELECT MONTH(b.bookingDate) as month, SUM(b.grandTotal) as total " +
            "FROM Booking b " +
            "WHERE YEAR(b.bookingDate) =:year " +
            "AND MONTH(b.bookingDate) BETWEEN :startMonth AND :endMonth " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.paymentStatus = 'PAID' " + // Lưu ý: Tôi sửa COMPLETED thành PAID cho khớp logic thanh toán
            "GROUP BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenue(int year, int startMonth, int endMonth);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate =:date")
    List<Booking> getBookingsToday(LocalDate date);
}