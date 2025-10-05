package com.humg.HotelSystemManagement.modules.employee_service.models.repositories;

import com.humg.HotelSystemManagement.modules.employee_service.models.entities.Employee;
import com.humg.HotelSystemManagement.modules.employee_service.models.entities.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Page<Attendance> findByEmployee(Employee employee, Pageable pageable);
}
