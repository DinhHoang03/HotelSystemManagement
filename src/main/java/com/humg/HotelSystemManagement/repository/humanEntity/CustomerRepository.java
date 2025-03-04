package com.humg.HotelSystemManagement.repository.humanEntity;

import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
