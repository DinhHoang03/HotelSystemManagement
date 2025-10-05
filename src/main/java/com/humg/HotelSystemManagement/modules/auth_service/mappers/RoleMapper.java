package com.humg.HotelSystemManagement.modules.auth_service.mappers;

import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.RoleRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.RoleResponse;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
