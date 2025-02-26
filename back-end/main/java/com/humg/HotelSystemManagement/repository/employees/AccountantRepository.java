package com.humg.HotelSystemManagement.repository.employees;

import com.humg.HotelSystemManagement.entity.employees.Accountant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountantRepository extends JpaRepository<Accountant, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
