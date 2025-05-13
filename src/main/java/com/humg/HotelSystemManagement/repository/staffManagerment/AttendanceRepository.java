package com.humg.HotelSystemManagement.repository.staffManagerment;

import com.humg.HotelSystemManagement.entity.User.Employee;
import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Page<Attendance> findByEmployee(Employee employee, Pageable pageable);
}
