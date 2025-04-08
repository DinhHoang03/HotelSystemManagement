package com.humg.HotelSystemManagement.controller.authourizezationController;

import com.humg.HotelSystemManagement.dto.request.security.authorization.RoleRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.security.authorizezation.RoleResponse;
import com.humg.HotelSystemManagement.service.SecurityService.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @PostMapping("/create")
    APIResponse<RoleResponse> createRole(@RequestBody RoleRequest request){
        return APIResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .message("Create role successfully")
                .build();
    }

    @GetMapping("/get-all")
    APIResponse<List<RoleResponse>> getAllRole(){
        return APIResponse.<List<RoleResponse>>builder()
                .result(roleService.getAllRole())
                .message("Successfully get all roles")
                .build();
    }

    @DeleteMapping("/del/{roleName}")
    APIResponse deleteRole(@PathVariable("roleName") String roleName){
        roleService.deleteRole(roleName);
        return APIResponse.builder()
                .message("Successfully deleted role " + roleName)
                .build();
    }
}
