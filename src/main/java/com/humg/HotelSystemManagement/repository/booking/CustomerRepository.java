package com.humg.HotelSystemManagement.repository.booking;

import com.humg.HotelSystemManagement.entity.booking.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
}
