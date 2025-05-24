package com.hotel.humg.HotelSystemManagement.repository.User;

import com.hotel.humg.HotelSystemManagement.entity.User.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
