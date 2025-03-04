package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.departmentHead.DepartmentHeadCreationRequest;
import com.humg.HotelSystemManagement.dto.request.departmentHead.DepartmentHeadUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.departmentHead.DepartmentHeadResponse;
import com.humg.HotelSystemManagement.service.HumanService.DepartmentHeadService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentHeadController {
    DepartmentHeadService departmentHeadService;

    @PostMapping("/register")
    APIResponse<DepartmentHeadResponse> createAdmin(@Valid @RequestBody DepartmentHeadCreationRequest request){
        return APIResponse.<DepartmentHeadResponse>builder()
                .result(departmentHeadService.create(request))
                .message("This admin account is successfully created")
                .build();
    }

    @PutMapping("/update/{adminId}")
    APIResponse<DepartmentHeadResponse> updateAdmin(@PathVariable("adminId") Long adminId, DepartmentHeadUpdateRequest request){
        return APIResponse.<DepartmentHeadResponse>builder()
                .result(departmentHeadService.updateById(adminId, request))
                .message("Update waiter successfully!")
                .build();
    }

    @DeleteMapping("/del/{adminId}")
    APIResponse<String> deleteAdmin(@PathVariable("adminId") Long adminId){
        departmentHeadService.deleteById(adminId);
        return APIResponse.<String>builder()
                .message("Delete admin number id " + adminId + " successfully!")
                .build();
    }
}
