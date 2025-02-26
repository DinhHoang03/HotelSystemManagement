package com.humg.HotelSystemManagement.repository.employees;

import com.humg.HotelSystemManagement.entity.employees.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
