package com.humg.HotelSystemManagement.repository.employees;

import com.humg.HotelSystemManagement.entity.employees.DepartmentHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentHeadRepository extends JpaRepository<DepartmentHead, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
