package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.humanEntity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    @Mapping(target = "password", ignore = true)
    Employee toEmployee(EmployeeCreationRequest request);

    EmployeeResponse toEmployeeResponse(Employee employee);

    void updateEmployee(@MappingTarget Employee employee, EmployeeUpdateRequest request);
}
