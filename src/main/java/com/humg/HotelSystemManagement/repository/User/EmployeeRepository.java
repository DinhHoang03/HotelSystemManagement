package com.humg.HotelSystemManagement.repository.User;

import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.entity.User.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByName(String name);
    List<Employee> findByUserStatusAndId(UserStatus userStatus, String id);
    Page<Employee> findByUserStatus(UserStatus userStatus, Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
