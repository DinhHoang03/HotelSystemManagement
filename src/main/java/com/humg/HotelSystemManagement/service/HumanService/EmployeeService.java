package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.enums.Gender;
import com.humg.HotelSystemManagement.entity.enums.Roles;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeService implements IGeneralHumanCRUDService<EmployeeResponse, EmployeeCreationRequest, EmployeeUpdateRequest> {

    EmployeeRepository employeeRepository;
    SecurityConfig securityConfig;

    public EmployeeResponse create(EmployeeCreationRequest request) {
        Employee employee;

        if (request != null) {

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
                .build();
    }

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
                        Employee.getRole().toString()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

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
                .build();

        return response;
    }

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
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        employeeRepository.delete(employee);
    }
}
