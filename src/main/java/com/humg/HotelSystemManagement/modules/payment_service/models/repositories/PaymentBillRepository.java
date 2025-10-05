package com.humg.HotelSystemManagement.modules.payment_service.models.repositories;

import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public interface PaymentBillRepository extends JpaRepository<PaymentBill, String> {

    @Query("SELECT SUM(p.paidAmount) FROM PaymentBill p WHERE DATE(p.createAt) = :date")
    Long getTodayRenevue(LocalDate date);
}