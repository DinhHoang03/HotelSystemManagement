package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.role.RoleRequest;
import com.humg.HotelSystemManagement.dto.response.authorizezation.RoleResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
