package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query("SELECT SUM(p.paidAmount) FROM Payment p WHERE DATE(p.createAt) = :date")
    Long getTodayRenevue(LocalDate date);
}