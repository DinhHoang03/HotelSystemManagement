package com.humg.HotelSystemManagement.service.SystemServices;

import com.humg.HotelSystemManagement.dto.request.role.RoleRequest;
import com.humg.HotelSystemManagement.dto.response.authorizezation.RoleResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import com.humg.HotelSystemManagement.mapper.RoleMapper;
import com.humg.HotelSystemManagement.repository.totalServices.PermissionRepository;
import com.humg.HotelSystemManagement.repository.totalServices.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse createRole(RoleRequest request){
        Role role = roleMapper.toRole(request);
        var permission = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permission));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAllRole(){
        var roles = roleRepository.findAll();

        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    public void deleteRole(String roleName){
        roleRepository.deleteById(roleName);
    }
}
