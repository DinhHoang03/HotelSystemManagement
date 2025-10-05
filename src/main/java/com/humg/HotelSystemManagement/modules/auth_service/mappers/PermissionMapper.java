package com.humg.HotelSystemManagement.modules.auth_service.mappers;

import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.PermissionRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.PermissionResponse;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
