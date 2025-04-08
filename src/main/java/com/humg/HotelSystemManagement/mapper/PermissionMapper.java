package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.security.authorization.PermissionRequest;
import com.humg.HotelSystemManagement.dto.response.security.authorizezation.PermissionResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
