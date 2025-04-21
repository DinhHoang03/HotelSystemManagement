package com.humg.HotelSystemManagement.repository.staffManagerment;

import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Page<Attendance> findByEmployee(Employee employee, Pageable pageable);
}
