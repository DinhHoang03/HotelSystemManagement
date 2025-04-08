package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.humanEntity.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.humanEntity.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    @Mapping(target = "password", ignore = true)
    Employee toEmployee(EmployeeCreationRequest request);

    EmployeeResponse toEmployeeResponse(Employee employee);

    @Mapping(target = "roles", ignore = true)
    void updateEmployee(@MappingTarget Employee employee, EmployeeUpdateRequest request);
}
