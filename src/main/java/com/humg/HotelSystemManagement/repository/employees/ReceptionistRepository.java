package com.humg.HotelSystemManagement.repository.employees;

import com.humg.HotelSystemManagement.entity.employees.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    boolean existsByEmail(String email);
}
