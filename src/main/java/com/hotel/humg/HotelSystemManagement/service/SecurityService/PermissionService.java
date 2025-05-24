package com.hotel.humg.HotelSystemManagement.service.SecurityService;

import com.hotel.humg.HotelSystemManagement.dto.request.security.authorization.PermissionRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.security.authorizezation.PermissionResponse;
import com.hotel.humg.HotelSystemManagement.entity.authorizezation.Permission;
import com.hotel.humg.HotelSystemManagement.mapper.PermissionMapper;
import com.hotel.humg.HotelSystemManagement.repository.authenticationRepository.PermissionRepository;
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
