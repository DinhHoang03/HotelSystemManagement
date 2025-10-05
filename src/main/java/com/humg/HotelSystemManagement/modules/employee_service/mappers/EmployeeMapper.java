package com.humg.HotelSystemManagement.modules.employee_service.mappers;

import com.humg.HotelSystemManagement.modules.employee_service.resources.requests.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.modules.employee_service.resources.responses.EmployeeResponse;
import com.humg.HotelSystemManagement.modules.employee_service.models.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    //@Mapping(target = "password", ignore = true)
    //Employee toEmployee(EmployeeCreationRequest request);

    EmployeeResponse toEmployeeResponse(Employee employee);

    @Mapping(target = "roles", ignore = true)
    void updateEmployee(@MappingTarget Employee employee, EmployeeUpdateRequest request);
}
