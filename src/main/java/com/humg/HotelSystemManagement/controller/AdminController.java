package com.humg.HotelSystemManagement.controller;

import com.humg.HotelSystemManagement.dto.request.admin.AdminCreationRequest;
import com.humg.HotelSystemManagement.dto.request.admin.AdminUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.admin.AdminResponse;
import com.humg.HotelSystemManagement.entity.employees.Admin;
import com.humg.HotelSystemManagement.service.AdminService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {
    AdminService adminService;

    @PostMapping("/create-account")
    APIResponse<Admin> createAdmin(@Valid @RequestBody AdminCreationRequest request){
        return APIResponse.<Admin>builder()
                .result(adminService.createAdmin(request))
                .message("This admin account is successfully created")
                .build();
    }

    @PutMapping("/update/{adminId}")
    APIResponse<AdminResponse> updateAdmin(@PathVariable("adminId") Long adminId, AdminUpdateRequest request){
        return APIResponse.<AdminResponse>builder()
                .result(adminService.updateUserById(adminId, request))
                .message("Update waiter successfully!")
                .build();
    }

    @DeleteMapping("/del/{adminId}")
    APIResponse<String> deleteAdmin(@PathVariable("adminId") Long adminId){
        adminService.deleteAdminById(adminId);
        return APIResponse.<String>builder()
                .message("Delete admin number id " + adminId + " successfully!")
                .build();
    }
}
