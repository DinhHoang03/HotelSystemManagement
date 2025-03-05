package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.service.HumanService.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {
    EmployeeService employeeService;

    @PostMapping("/register")
    APIResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreationRequest request){
        return APIResponse.<EmployeeResponse>builder()
                .result(employeeService.create(request))
                .message("Your employee account is successfully created!")
                .build();
    }

    @PutMapping("/update/{empId}")
    APIResponse<EmployeeResponse> updateEmployee(@PathVariable("empId") Long empId, @Valid @RequestBody EmployeeUpdateRequest request){
        return APIResponse.<EmployeeResponse>builder()
                .result(employeeService.updateById(empId, request))
                .message("Update waiter successfully!")
                .build();
    }

    @DeleteMapping("/del/{empId}")
    APIResponse<String> deleteEmployee(@PathVariable("empId") Long empId){
        employeeService.deleteById(empId);
        return APIResponse.<String>builder()
                .message("Delete waiter number id " + empId + " successfully!")
                .build();
    }
}
