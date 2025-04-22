package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Page<Booking> findByCustomer_Id(String customerId, Pageable pageable);

    @Query("SELECT MONTH(b.bookingDate) as month, SUM(b.grandTotal) as total " +
            "FROM Booking b " +
            "WHERE YEAR(b.bookingDate) =:year " +
            "AND MONTH(b.bookingDate) BETWEEN :startMonth AND :endMonth " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.paymentStatus = 'COMPLETED' " +
            "GROUP BY MONTH(b.bookingDate)")
    List<Object[]> findMonthlyRevenue(int year, int startMonth, int endMonth);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate =:date")
    List<Booking> getBookingsToday(LocalDate date);
}
