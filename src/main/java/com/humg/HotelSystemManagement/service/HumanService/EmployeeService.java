package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.security.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.EmployeeMapper;
import com.humg.HotelSystemManagement.repository.humanEntity.EmployeeRepository;
import com.humg.HotelSystemManagement.repository.authenticationRepository.RoleRepository;
import com.humg.HotelSystemManagement.service.IGeneralCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeService implements IGeneralCRUDService<EmployeeResponse, EmployeeCreationRequest, EmployeeUpdateRequest, String> {

    EmployeeRepository employeeRepository;
    RoleRepository roleRepository;
    EmployeeMapper employeeMapper;
    SecurityConfig securityConfig;

    @PreAuthorize("!hasRole('CUSTOMER') and !hasRole('ADMIN')")
    public EmployeeResponse create(EmployeeCreationRequest request) {
        Employee employee;

        if (request != null) {
            /**
            Roles requestedRole = Roles.valueOf(request.getRole().toUpperCase());
            if(requestedRole.equals(Roles.ADMIN)){
                throw new AppException(AppErrorCode.ADMIN_CREATION_NOT_ALLOWED);
            }
            */

            if (employeeRepository.existsByEmail(request.getEmail()) ||
                    employeeRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            employee = employeeMapper.toEmployee(request);

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());
            employee.setPassword(encodedPassword);


        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        employee = employeeRepository.save(employee);

        return employeeMapper.toEmployeeResponse(employee);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAuthority('GET_ALL_EMPLOYEE')")
    public List<EmployeeResponse> getAll() {
        List<EmployeeResponse> list = employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toEmployeeResponse)
                .toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    public Page<EmployeeResponse> getAll(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        return employeePage.map(employeeMapper::toEmployeeResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse getById(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return employeeMapper.toEmployeeResponse(employee);
    }

    public EmployeeResponse getMyInfo(){

        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return employeeMapper.toEmployeeResponse(employee);
    }

    @PreAuthorize("!hasRole('ADMIN') and !hasRole('CUSTOMER')")
    public EmployeeResponse update(String id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            employeeMapper.updateEmployee(employee ,request);
            var updatedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            var roles = roleRepository.findAllById(request.getRoles());
            employee.setRoles(new HashSet<>(roles));

        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Employee updatedEmployee = employeeRepository.save(employee);

        return employeeMapper.toEmployeeResponse(updatedEmployee);
    }


    public void delete(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        employeeRepository.delete(employee);
    }
}
