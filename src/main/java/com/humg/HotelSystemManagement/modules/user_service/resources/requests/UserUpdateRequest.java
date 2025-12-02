package com.humg.HotelSystemManagement.modules.user_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String username; // Có thể cho sửa hoặc không
    String password;
    String name;
    String email;
    String phone;
    String identityId;
    String address;
    LocalDate dob;
    String gender;
    String userStatus; // "ACTIVE", "INACTIVE"

    List<String> roles; // <--- MỚI THÊM: Danh sách tên quyền (VD: ["ADMIN"], ["USER"])
}