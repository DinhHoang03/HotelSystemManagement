package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.departmentHead.DepartmentHeadCreationRequest;
import com.humg.HotelSystemManagement.dto.request.departmentHead.DepartmentHeadUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.departmentHead.DepartmentHeadResponse;
import com.humg.HotelSystemManagement.entity.employees.DepartmentHead;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.DepartmentHeadRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentHeadService implements IGeneralHumanCRUDService<DepartmentHeadResponse, DepartmentHeadCreationRequest, DepartmentHeadUpdateRequest> {
    DepartmentHeadRepository departmentHeadRepository;
    SecurityConfig securityConfig;

    public DepartmentHeadResponse create(DepartmentHeadCreationRequest request) {
        DepartmentHead departmentHead;

        if (request != null) {

            if (departmentHeadRepository.existsByEmail(request.getEmail()) ||
                    departmentHeadRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            departmentHead = DepartmentHead.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role("departmentHead")
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        departmentHead = departmentHeadRepository.save(departmentHead);
        return DepartmentHeadResponse.builder()
                .id(departmentHead.getId())
                .name(departmentHead.getName())
                .phone(departmentHead.getPhone())
                .email(departmentHead.getEmail())
                .role(departmentHead.getRole())
                .build();
    }

    public List<DepartmentHeadResponse> getAll() {
        List<DepartmentHeadResponse> list = departmentHeadRepository.findAll()
                .stream()
                .map(departmentHead -> new DepartmentHeadResponse(
                        departmentHead.getId(),
                        departmentHead.getName(),
                        departmentHead.getEmail(),
                        departmentHead.getPhone(),
                        departmentHead.getRole()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public DepartmentHeadResponse getById(Long id) {
        DepartmentHead departmentHead = departmentHeadRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        DepartmentHeadResponse response = DepartmentHeadResponse.builder()
                .id(departmentHead.getId())
                .name(departmentHead.getName())
                .phone(departmentHead.getPhone())
                .email(departmentHead.getEmail())
                .role(departmentHead.getRole())
                .build();

        return response;
    }

    public DepartmentHeadResponse updateById(Long id, DepartmentHeadUpdateRequest request) {
        DepartmentHead departmentHead = departmentHeadRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_EXISTED));

        if (request != null) {
            departmentHead.setEmail(request.getEmail());
            departmentHead.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        DepartmentHead updateddepartmentHead = departmentHeadRepository.save(departmentHead);

        DepartmentHeadResponse result = DepartmentHeadResponse.builder()
                .id(updateddepartmentHead.getId())
                .name(updateddepartmentHead.getName())
                .email(updateddepartmentHead.getEmail())
                .phone(updateddepartmentHead.getPhone())
                .role(updateddepartmentHead.getRole())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        DepartmentHead departmentHead = departmentHeadRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        departmentHeadRepository.delete(departmentHead);
    }
}
