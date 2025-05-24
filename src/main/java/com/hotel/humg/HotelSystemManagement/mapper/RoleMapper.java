package com.hotel.humg.HotelSystemManagement.mapper;

import com.hotel.humg.HotelSystemManagement.dto.request.security.authorization.RoleRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.security.authorizezation.RoleResponse;
import com.hotel.humg.HotelSystemManagement.entity.authorizezation.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
