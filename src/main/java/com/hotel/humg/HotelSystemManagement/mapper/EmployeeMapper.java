package com.hotel.humg.HotelSystemManagement.mapper;

import com.hotel.humg.HotelSystemManagement.dto.request.user.employee.EmployeeUpdateRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.user.employee.EmployeeResponse;
import com.hotel.humg.HotelSystemManagement.entity.User.Employee;
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
