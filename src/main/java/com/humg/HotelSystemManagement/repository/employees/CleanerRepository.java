package com.humg.HotelSystemManagement.repository.employees;

import com.humg.HotelSystemManagement.entity.employees.Cleaner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleanerRepository extends JpaRepository<Cleaner, Long> {

}
