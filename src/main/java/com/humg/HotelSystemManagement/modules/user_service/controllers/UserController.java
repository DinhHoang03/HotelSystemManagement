package com.humg.HotelSystemManagement.modules.user_service.controllers;

import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.user_service.resources.requests.UserUpdateRequest;
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    // --- 1. LẤY THỐNG KÊ (Cho Dashboard Cards) ---
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    public APIResponse<Map<String, Long>> getUserStats() {
        return APIResponse.<Map<String, Long>>builder()
                .result(userService.getUserStats())
                .message("Get user statistics successfully")
                .build();
    }

    // --- 2. LẤY DANH SÁCH (Phân trang + Tìm kiếm) ---
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    public APIResponse<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return APIResponse.<Page<UserResponse>>builder()
                .result(userService.getUsers(keyword, page, size))
                .message("Get user list successfully")
                .build();
    }

    // --- 3. TẠO MỚI ---
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('USER_CREATE')") // Hoặc quyền SYSTEM_MANAGE
    APIResponse<UserResponse> createCustomer(@Valid @RequestBody UserCreationRequest request){
        return APIResponse.<UserResponse>builder()
                .result(userService.create(request))
                .message("The user account is successfully created!")
                .build();
    }

    // --- 4. CẬP NHẬT ---
    @PutMapping("/update/{userId}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasAuthority('SYSTEM_MANAGE')")
    APIResponse<UserResponse> updateCustomer(@PathVariable("userId") String userId, @RequestBody UserUpdateRequest request){
        return APIResponse.<UserResponse>builder()
                .result(userService.update(userId, request))
                .message("Update user information successfully")
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    APIResponse<UserResponse> getById(@PathVariable("userId") String userId){
        return APIResponse.<UserResponse>builder()
                .result(userService.getById(userId))
                .message("Update user information successfully")
                .build();
    }

    // --- 5. XÓA ---
    @DeleteMapping("/del/{userId}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasAuthority('SYSTEM_MANAGE')")
    APIResponse<String> deleteCustomer(@PathVariable("userId") String userId){
        userService.delete(userId);
        return APIResponse.<String>builder()
                .message("Delete user id " + userId + " successfully!")
                .build();
    }

    // --- 6. INFO CÁ NHÂN ---
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('USER_PROFILE')")
    UserResponse getMyInfo() {
        return userService.getMyInfo();
    }
}