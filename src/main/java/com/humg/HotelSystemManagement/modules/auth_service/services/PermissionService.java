package com.humg.HotelSystemManagement.modules.auth_service.services;

import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.PermissionRequest;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.PermissionResponse;
import com.humg.HotelSystemManagement.modules.auth_service.models.entities.Permission;
import com.humg.HotelSystemManagement.modules.auth_service.mappers.PermissionMapper;
import com.humg.HotelSystemManagement.modules.auth_service.models.repositories.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAllPermission(){
        var permissions = permissionRepository.findAll();

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    public void deletePermission(String permissionName){
        permissionRepository.deleteById(permissionName);
    }
}
