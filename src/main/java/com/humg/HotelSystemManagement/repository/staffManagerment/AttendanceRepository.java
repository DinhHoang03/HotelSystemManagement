package com.humg.HotelSystemManagement.repository.staffManagerment;

import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
}
