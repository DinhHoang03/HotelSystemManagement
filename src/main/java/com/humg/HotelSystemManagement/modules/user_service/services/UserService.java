package com.humg.HotelSystemManagement.modules.user_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.RoleRepository;
import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.user_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserUpdateRequest;
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.utils.enums.Gender;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    // --- 1. CREATE (Giữ nguyên logic cũ của bạn) ---
    public UserResponse create(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByPhone(request.getPhone()) || userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .identityId(request.getIdentityId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .dob(request.getDob())
                .address(request.getAddress())
                .gender(Gender.valueOf(request.getGender()))
                .userStatus(UserStatus.ENABLED)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        var customerRole = roleRepository.findById("CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("CUSTOMER", "Customer Role", new HashSet<>())));
        user.setRoles(new HashSet<>(Set.of(customerRole)));

        return mapToUserResponse(userRepository.save(user));
    }

    // --- 2. UPDATE (Đã nâng cấp: Update Role & Status) ---
    public UserResponse update(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        // Update thông tin cơ bản
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getIdentityId() != null) user.setIdentityId(request.getIdentityId());

        // Update Status (Active/Inactive)
        if (request.getUserStatus() != null) {
            try {
                user.setUserStatus(UserStatus.valueOf(request.getUserStatus()));
            } catch (IllegalArgumentException e) {
                // Log warning or ignore
            }
        }

        // Update Roles (QUAN TRỌNG CHO ADMIN)
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        return mapToUserResponse(userRepository.save(user));
    }

    public UserResponse getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return mapToUserResponse(user);
    }

    // --- 3. GET STATS (MỚI: Cho 3 cái thẻ bài thống kê) ---
    public Map<String, Long> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByUserStatus(UserStatus.ENABLED);
        long admins = userRepository.countByRoles_Name("ADMIN"); // Hoặc tên role admin trong DB của bạn

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("admins", admins);
        return stats;
    }

    // --- 4. GET LIST & SEARCH (MỚI: Cho bảng dữ liệu) ---
    public Page<UserResponse> getUsers(String keyword, int page, int size) {
        // Sắp xếp theo ngày tạo mới nhất (nếu User có field createdAt, ko thì sort theo username)
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> userPage = userRepository.searchUsers(keyword, pageable);
        return userPage.map(this::mapToUserResponse);
    }

    // --- 5. DELETE (Giữ nguyên) ---
    public void delete(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));
        user.getRoles().clear();
        userRepository.save(user);
        userRepository.delete(user);
    }

    // --- 6. GET MY INFO (Giữ nguyên) ---
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));
        return mapToUserResponse(user);
    }

    // --- Helper Mapper ---
    private UserResponse mapToUserResponse(User user) {
        if (user == null) return null;
        String roleName = user.getRoles().stream().findFirst().map(Role::getName).orElse("UNKNOWN");
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dob(user.getDob())
                .identityId(user.getIdentityId())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .userStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null)
                .role(roleName)
                .build();
    }
}