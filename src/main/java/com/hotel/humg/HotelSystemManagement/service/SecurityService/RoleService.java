package com.hotel.humg.HotelSystemManagement.service.SecurityService;

import com.hotel.humg.HotelSystemManagement.dto.request.security.authorization.RoleRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.security.authorizezation.RoleResponse;
import com.hotel.humg.HotelSystemManagement.entity.authorizezation.Role;
import com.hotel.humg.HotelSystemManagement.mapper.RoleMapper;
import com.hotel.humg.HotelSystemManagement.repository.authenticationRepository.PermissionRepository;
import com.hotel.humg.HotelSystemManagement.repository.authenticationRepository.RoleRepository;
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

    public RoleResponse createRole(RoleRequest request) {
        Role role = roleMapper.toRole(request);

        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            role.setPermissions(new HashSet<>()); // hoặc ném lỗi tùy bạn muốn
        } else {
            var permissions = permissionRepository.findAllById(request.getPermissions());
            role.setPermissions(new HashSet<>(permissions));
        }

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
