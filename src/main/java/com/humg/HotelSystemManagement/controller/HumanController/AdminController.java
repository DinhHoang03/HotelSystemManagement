package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.user.customer.CustomerResponse;
import com.humg.HotelSystemManagement.dto.response.user.employee.AttendanceResponse;
import com.humg.HotelSystemManagement.dto.response.user.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.service.EmployeeService.AttendanceService;
import com.humg.HotelSystemManagement.service.UserService.AdminService;
import com.humg.HotelSystemManagement.service.UserService.CustomerService;
import com.humg.HotelSystemManagement.service.UserService.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    CustomerService customerService;
    EmployeeService employeeService;
    AdminService adminService;
    AttendanceService attendanceService;

    @GetMapping("/get-customer/{customerId}")
    APIResponse<CustomerResponse> getCustomerById(@PathVariable("customerId") String customerId){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getById(customerId))
                .message("Successfully get user by follow id!")
                .build();
    }

    //Get all sort by pages
    @GetMapping("/get-customers/list")
    APIResponse<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<
                        Page<CustomerResponse>
                        >builder()
                .result(customerService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    //Get all sort by pages
    @GetMapping("/get-employees/list")
    APIResponse<Page<EmployeeResponse>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<Page<EmployeeResponse>>builder()
                .result(employeeService.getAll(page, size))
                .message("Successfully get all employees!")
                .build();
    }

    @GetMapping("/get-employee/{employeeId}")
    APIResponse<EmployeeResponse> getEmployeeById(@PathVariable("employeeId") String employeeId){
        return APIResponse.<EmployeeResponse>builder()
                .result(employeeService.getById(employeeId))
                .message("Successfully get user by follow id!")
                .build();
    }

    @GetMapping("/get-attendances/list")
    APIResponse<Page<AttendanceResponse>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<AttendanceResponse>>builder()
                .result(attendanceService.getAllAttendances(page, size))
                .message("Get all attendances")
                .build();
    }

    @PostMapping("/approve/{empId}")
    APIResponse<EmployeeResponse> approveEmp(@PathVariable("empId") String id) {
        return APIResponse.<EmployeeResponse>builder()
                .result(adminService.approveEmployee(id))
                .message("Approve emp " + id + " successfully")
                .build();
    }

    @PostMapping("/reject/{empId}")
    APIResponse<EmployeeResponse> reject(@PathVariable("empId") String id) {
        return APIResponse.<EmployeeResponse>builder()
                .result(adminService.rejectEmployee(id))
                .message("Reject emp " + id + " successfully")
                .build();
    }

    @GetMapping("/get-status/{status}")
    APIResponse<Page<EmployeeResponse>> findAllByStatusEmp(@PathVariable("status")UserStatus userStatus,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size){
        return APIResponse.<Page<EmployeeResponse>>builder()
                .result(adminService.findAllByStatusEmployee(page, size, userStatus))
                .message("Successfully get all employees by status!")
                .build();
    }

    @GetMapping("/count-employees")
    APIResponse<Long> countEmp() {
        return APIResponse.<Long>builder()
                .result(adminService.countEmployeeByList())
                .message("Get count successfully")
                .build();
    }

    @GetMapping("/count-customers")
    APIResponse<Long> countCus() {
        return APIResponse.<Long>builder()
                .result(adminService.countCustomerByList())
                .message("Get count successfully")
                .build();
    }

    @GetMapping("/count-rooms")
    APIResponse<Long> countRoom() {
        return APIResponse.<Long>builder()
                .result(adminService.countRoomByList())
                .message("Get count successfully")
                .build();
    }

    @GetMapping("/today-bookings")
    APIResponse<Long> countBooking(LocalDate date) {
        return APIResponse.<Long>builder()
                .result(adminService.countBookingTodayByList(date))
                .message("Get count successfully")
                .build();
    }

    @GetMapping("/today-revenue")
    APIResponse<Long> getTodayRevenue(LocalDate now) {
        return APIResponse.<Long>builder()
                .result(adminService.getTodayRevenue(now))
                .message("Get count successfully")
                .build();
    }

    @GetMapping("total-users")
    APIResponse<Long> totalUsers() {
        return APIResponse.<Long>builder()
                .result(adminService.totalCountUser())
                .message("Get total count complete")
                .build();
    }

    @GetMapping("/revenue")
    APIResponse<Map<String, Long>> getRevenue(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "startMonth") int startMonth,
            @RequestParam(name = "endMonth") int endMonth
    ){
        return APIResponse.<Map<String, Long>>builder()
                .result(adminService.getMonthlyRevenue(year, startMonth, endMonth))
                .message("Get revenue successfully")
                .build();
    }

    @GetMapping("/room-occupancy")
    APIResponse<Map<String, Double>> getWeeklyOccupancyRate() {
        return APIResponse.<Map<String, Double>>builder()
                .result(adminService.calculateOccupancyRateForWeek())
                .message("Get occupancy rate successfully")
                .build();
    }
}
