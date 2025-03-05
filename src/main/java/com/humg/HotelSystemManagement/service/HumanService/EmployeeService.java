package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.enums.Gender;
import com.humg.HotelSystemManagement.entity.enums.Roles;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeService implements IGeneralHumanCRUDService<EmployeeResponse, EmployeeCreationRequest, EmployeeUpdateRequest> {

    EmployeeRepository employeeRepository;
    SecurityConfig securityConfig;

    @PreAuthorize("!hasRole('CUSTOMER')")
    public EmployeeResponse create(EmployeeCreationRequest request) {
        Employee employee;

        if (request != null) {

            Roles requestedRole = Roles.valueOf(request.getRole().toUpperCase());
            if(requestedRole.equals(Roles.ADMIN)){
                throw new AppException(AppErrorCode.ADMIN_CREATION_NOT_ALLOWED);
            }

            if (employeeRepository.existsByEmail(request.getEmail()) ||
                    employeeRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            employee = Employee.builder()
                    .name(request.getName())
                    .identityId(request.getIdentityId())
                    .email(request.getEmail())
                    .dob(request.getDob())
                    .phone(request.getPhone())
                    .password(encodedPassword)
                    .gender(Gender.valueOf(request.getGender().toUpperCase()))
                    .role(Roles.valueOf(request.getRole().toUpperCase()))
                    .username(request.getUsername())
                    .userStatus(UserStatus.PENDING)
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        employee = employeeRepository.save(employee);

        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .username(employee.getUsername())
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .role(employee.getRole().toString())
                .identityId(employee.getIdentityId())
                .dob(employee.getDob())
                .gender(employee.getGender().toString())
                .userStatus(employee.getUserStatus().toString())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeResponse> getAll() {
        List<EmployeeResponse> list = employeeRepository.findAll()
                .stream()
                .map(Employee -> new EmployeeResponse(
                        Employee.getId(),
                        Employee.getUsername(),
                        Employee.getName(),
                        Employee.getGender().toString(),
                        Employee.getDob(),
                        Employee.getEmail(),
                        Employee.getPhone(),
                        Employee.getIdentityId(),
                        Employee.getUserStatus().toString(),
                        Employee.getRole().toString()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        EmployeeResponse response = EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .username(employee.getUsername())
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .role(employee.getRole().toString())
                .identityId(employee.getIdentityId())
                .dob(employee.getDob())
                .gender(employee.getGender().toString())
                .userStatus(employee.getUserStatus().toString())
                .build();

        return response;
    }

    public EmployeeResponse getMyInfo(){

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .username(employee.getUsername())
                .phone(employee.getPhone())
                .email(employee.getEmail())
                .role(employee.getRole().toString())
                .identityId(employee.getIdentityId())
                .dob(employee.getDob())
                .gender(employee.getGender().toString())
                .userStatus(employee.getUserStatus().toString())
                .build();
    }

    @PreAuthorize("!hasRole('ADMIN') and !hasRole('CUSTOMER')")
    public EmployeeResponse updateById(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            employee.setUsername(request.getUsername());
            employee.setEmail(request.getEmail());
            employee.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        EmployeeResponse result = EmployeeResponse.builder()
                .id(updatedEmployee.getId())
                .name(updatedEmployee.getName())
                .username(updatedEmployee.getUsername())
                .phone(updatedEmployee.getPhone())
                .email(updatedEmployee.getEmail())
                .role(updatedEmployee.getRole().toString())
                .identityId(updatedEmployee.getIdentityId())
                .dob(updatedEmployee.getDob())
                .gender(updatedEmployee.getGender().toString())
                .userStatus(updatedEmployee.getUserStatus().toString())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        employeeRepository.delete(employee);
    }
}
