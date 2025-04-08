package com.humg.HotelSystemManagement.controller.authourizezationController;

import com.humg.HotelSystemManagement.dto.request.security.authorization.PermissionRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.security.authorizezation.PermissionResponse;
import com.humg.HotelSystemManagement.service.SecurityService.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping("/create")
    APIResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest request){
        return APIResponse.<PermissionResponse>builder()
                .result(permissionService.createPermission(request))
                .message("Create permission successfully")
                .build();
    }

    @GetMapping("/get-all")
    APIResponse<List<PermissionResponse>> getAllPermissions(){
        return APIResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAllPermission())
                .message("Successfully get all permissions")
                .build();
    }

    @DeleteMapping("/del/{permissionName}")
    APIResponse deletePermission(@RequestParam("permissionName") String permissionName){
        permissionService.deletePermission(permissionName);

        return APIResponse.builder()
                .message("Delete permission " + permissionName + " successfully")
                .build();
    }
}
