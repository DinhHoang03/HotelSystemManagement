package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByUser(User user, Pageable pageable);

    List<Booking> findTop5ByOrderByBookingDateDesc();

    // Query thống kê này không dính đến Customer nên giữ nguyên
    @Query("SELECT MONTH(b.bookingDate) as month, SUM(b.grandTotal) as total " +
            "FROM Booking b " +
            "WHERE YEAR(b.bookingDate) =:year " +
            "AND MONTH(b.bookingDate) BETWEEN :startMonth AND :endMonth " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.paymentStatus = 'PAID' " + // Lưu ý: Tôi sửa COMPLETED thành PAID cho khớp logic thanh toán
            "GROUP BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenue(int year, int startMonth, int endMonth);

    // Hàm đếm booking trong ngày
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date")
    long countByBookingDate(@Param("date") LocalDate date);

    // 1. Tổng doanh thu (Chỉ tính đơn đã thanh toán COMPLETED)
    @Query("SELECT SUM(b.grandTotal) FROM Booking b WHERE b.paymentStatus = 'COMPLETED'")
    Double calculateTotalRevenue();

    // 2. Doanh thu theo tháng (Chart Analysis)
    @Query("SELECT MONTH(b.bookingDate) as month, SUM(b.grandTotal) as total " +
            "FROM Booking b " +
            "WHERE YEAR(b.bookingDate) = :year AND b.paymentStatus = 'COMPLETED' " +
            "GROUP BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenueByYear(@Param("year") int year);

    Page<Booking> findByUserAndBookingStatus(User user, BookingStatus status, Pageable pageable);

    // 3. Thống kê Booking theo loại phòng (Donut Chart)
    // Booking -> BookingRoom -> rooms (ManyToMany) -> RoomType
    @Query("SELECT rt.roomTypes, COUNT(b) " +
            "FROM Booking b " +
            "JOIN b.bookingRooms br " +
            "JOIN br.rooms r " +
            "JOIN r.roomType rt " +
            "WHERE b.paymentStatus = 'COMPLETED' " +
            "GROUP BY rt.roomTypes")
    List<Object[]> countBookingsByRoomType();

    // 4. Đếm tổng số booking thành công
    long countByPaymentStatus(PaymentStatus status);
}