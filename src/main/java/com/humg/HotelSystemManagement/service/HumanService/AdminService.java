package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.dto.request.admin.FindEmpStatusRequest;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.EmployeeMapper;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {

    EmployeeRepository employeeRepository;
    EmployeeMapper employeeMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse approveEmployee(String id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(employee.getUserStatus() == UserStatus.PENDING || employee.getUserStatus() == UserStatus.REJECTED){
            employee.setUserStatus(UserStatus.APPROVED);
            employeeRepository.save(employee);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return employeeMapper.toEmployeeResponse(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse rejectEmployee(String id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(employee.getUserStatus() == UserStatus.PENDING || employee.getUserStatus() == UserStatus.APPROVED){
            employee.setUserStatus(UserStatus.REJECTED);
            employeeRepository.save(employee);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return employeeMapper.toEmployeeResponse(employee);
    }

    public Page<EmployeeResponse> findAllByStatusEmployee(int page, int size, UserStatus userStatus) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Employee> employees = employeeRepository.findByUserStatus(userStatus, pageable);
        if(employees.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        Page<EmployeeResponse> response = employees.map(employee -> {
            return EmployeeResponse.builder()
                    .id(employee.getId())
                    .username(employee.getUsername())
                    .name(employee.getName())
                    .gender(employee.getGender().toString())
                    .dob(employee.getDob())
                    .email(employee.getEmail())
                    .phone(employee.getPhone())
                    .address(employee.getAddress())
                    .identityId(employee.getIdentityId())
                    .userStatus(employee.getUserStatus().toString())
                    .build();
        });
        return response;
    }
}
