package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.permission.PermissionRequest;
import com.humg.HotelSystemManagement.dto.response.authorizezation.PermissionResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
