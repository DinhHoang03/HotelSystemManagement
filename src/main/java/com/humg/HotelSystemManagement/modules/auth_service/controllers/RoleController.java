package com.humg.HotelSystemManagement.modules.auth_service.controllers;

import com.humg.HotelSystemManagement.modules.auth_service.resources.requests.RoleRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.auth_service.resources.responses.RoleResponse;
import com.humg.HotelSystemManagement.modules.auth_service.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    APIResponse<RoleResponse> createRole(@RequestBody RoleRequest request){
        return APIResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .message("Create role successfully")
                .build();
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    APIResponse<List<RoleResponse>> getAllRole(){
        return APIResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRole())
                .message("Successfully get all roles")
                .build();
    }

    @DeleteMapping("/del/{roleName}")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    APIResponse deleteRole(@PathVariable("roleName") String roleName){
        roleService.deleteRole(roleName);
        return APIResponse.builder()
                .message("Successfully deleted role " + roleName)
                .build();
    }
}
