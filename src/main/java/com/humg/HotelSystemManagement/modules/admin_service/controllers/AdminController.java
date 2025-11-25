package com.humg.HotelSystemManagement.modules.admin_service.controllers;

import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.admin_service.services.AdminService;
import com.humg.HotelSystemManagement.modules.customer_service.services.UserService;
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
    UserService userService;
    AdminService adminService;

    @PostMapping("/approve/{empId}")
    APIResponse<String> approveEmp(@PathVariable("empId") String id) {
        return APIResponse.<String>builder()
                .result(adminService.enableUser(id))
                .build();
    }

    @PostMapping("/reject/{empId}")
    APIResponse<String> reject(@PathVariable("empId") String id) {
        return APIResponse.<String>builder()
                .result(adminService.disableUser(id))
                .build();
    }

    @GetMapping("/get-customer/{customerId}")
    APIResponse<UserResponse> getCustomerById(@PathVariable("customerId") String customerId){
        return APIResponse.<UserResponse>builder()
                .result(userService.getById(customerId))
                .message("Successfully get user by follow id!")
                .build();
    }

    //Get all sort by pages
    @GetMapping("/get-customers/list")
    APIResponse<Page<UserResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<
                        Page<UserResponse>
                        >builder()
                .result(userService.getAll(page, size))
                .message("Successfully get all customers!")
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
