package com.hotel.humg.HotelSystemManagement.repository.booking;

import com.hotel.humg.HotelSystemManagement.entity.booking.BookingBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingBillRepository extends JpaRepository<BookingBill, String> {
    @Query("SELECT b FROM BookingBill b WHERE b.booking.customer.id = :customerId")
    Page<BookingBill> findCustomerIdByBookingBillId(@Param("customerId") String customerId, Pageable pageable);
}
