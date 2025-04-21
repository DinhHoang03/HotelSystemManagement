package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.employee.EmployeeCreationRequest;
import com.humg.HotelSystemManagement.dto.request.employee.EmployeeUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.AttendanceResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.CheckInResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.CheckOutResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.service.EmployeeService.AttendanceService;
import com.humg.HotelSystemManagement.service.HumanService.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {
    EmployeeService employeeService;
    AttendanceService attendanceService;

    @PostMapping("/register")
    APIResponse<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeCreationRequest request){
        return APIResponse.<EmployeeResponse>builder()
                .result(employeeService.create(request))
                .message("Your employee account is successfully created!")
                .build();
    }

    @PutMapping("/update/{empId}")
    APIResponse<EmployeeResponse> updateEmployee(@PathVariable("empId") String empId, @Valid @RequestBody EmployeeUpdateRequest request){
        return APIResponse.<EmployeeResponse>builder()
                .result(employeeService.update(empId, request))
                .message("Update waiter successfully!")
                .build();
    }

    @DeleteMapping("/del/{empId}")
    APIResponse<String> deleteEmployee(@PathVariable("empId") String empId){
        employeeService.delete(empId);
        return APIResponse.<String>builder()
                .message("Delete waiter number id " + empId + " successfully!")
                .build();
    }

    @PostMapping("/check-in")
    APIResponse<CheckInResponse> checkIn(@AuthenticationPrincipal Jwt principal) {
        var username = principal.getSubject();
        return APIResponse.<CheckInResponse>builder()
                .result(attendanceService.checkIn(username))
                .message("Check in successfully")
                .build();
    }

    @PostMapping("/check-out")
    APIResponse<CheckOutResponse> checkOut(@AuthenticationPrincipal Jwt principal) {
        var username = principal.getSubject();
        return APIResponse.<CheckOutResponse>builder()
                .result(attendanceService.checkOut(username))
                .message("Check out successfully")
                .build();
    }

    @PostMapping("/attendance/create")
    APIResponse<AttendanceResponse> createAttendance(@AuthenticationPrincipal Jwt principal) {
        var username = principal.getSubject();
        return APIResponse.<AttendanceResponse>builder()
                .result(attendanceService.createAttendance(username))
                .message("Create attendance successfully")
                .build();
    }

    @GetMapping("/list")
    APIResponse<Page<AttendanceResponse>> getMyAttendance(
            @AuthenticationPrincipal Jwt principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var username = principal.getSubject();
        return APIResponse.<Page<AttendanceResponse>>builder()
                .result(attendanceService.getMyAttendances(username, page, size))
                .message("Get my attendance successfully")
                .build();
    }
}
