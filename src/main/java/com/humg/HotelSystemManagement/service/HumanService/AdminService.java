package com.humg.HotelSystemManagement.service.HumanService;

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

        if(employee.getUserStatus() == UserStatus.PENDING){
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

        if(employee.getUserStatus() == UserStatus.PENDING){
            employee.setUserStatus(UserStatus.REJECTED);
            employeeRepository.save(employee);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return employeeMapper.toEmployeeResponse(employee);
    }
}
