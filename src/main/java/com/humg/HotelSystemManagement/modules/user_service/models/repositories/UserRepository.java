package com.humg.HotelSystemManagement.modules.user_service.models.repositories;

import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // --- MỚI THÊM: Đếm cho phần Thống kê ---
    long countByUserStatus(UserStatus status); // Đếm user đang Active
    long countByRoles_Name(String roleName);   // Đếm số lượng Admin

    // --- MỚI THÊM: Tìm kiếm User (theo tên, email, hoặc sđt) ---
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(String keyword, Pageable pageable);

    @Query("SELECT u.dob FROM User u WHERE u.userStatus = 'ENABLED' AND u.dob IS NOT NULL")
    List<LocalDate> findAllDoB();
}