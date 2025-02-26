package com.humg.HotelSystemManagement.repository.staffManagerment;

import com.humg.HotelSystemManagement.entity.staffManagerment.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
}
