package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.admin.AdminCreationRequest;
import com.humg.HotelSystemManagement.dto.request.admin.AdminUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.admin.AdminResponse;
import com.humg.HotelSystemManagement.entity.employees.Admin;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.AdminRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService implements IGeneralHumanCRUDService<AdminResponse, AdminCreationRequest, AdminUpdateRequest> {
    AdminRepository adminRepository;
    SecurityConfig securityConfig;

    public AdminResponse create(AdminCreationRequest request) {
        Admin admin;

        if (request != null) {

            if (adminRepository.existsByEmail(request.getEmail()) ||
                    adminRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            admin = Admin.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        admin = adminRepository.save(admin);
        return AdminResponse.builder()
                .adminId(admin.getId())
                .name(admin.getName())
                .phone(admin.getPhone())
                .email(admin.getEmail())
                .build();
    }

    public List<AdminResponse> getAll() {
        List<AdminResponse> list = adminRepository.findAll()
                .stream()
                .map(admin -> new AdminResponse(
                        admin.getId(),
                        admin.getName(),
                        admin.getEmail(),
                        admin.getPhone()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public AdminResponse getById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        AdminResponse response = AdminResponse.builder()
                .adminId(admin.getId())
                .name(admin.getName())
                .phone(admin.getPhone())
                .email(admin.getEmail())
                .build();

        return response;
    }

    public AdminResponse updateById(Long id, AdminUpdateRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_EXISTED));

        if (request != null) {
            admin.setEmail(request.getEmail());
            admin.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Admin updatedAdmin = adminRepository.save(admin);

        AdminResponse result = AdminResponse.builder()
                .adminId(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        adminRepository.delete(admin);
    }
}
