package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.customer.CustomerResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.service.HumanService.CustomerService;
import com.humg.HotelSystemManagement.service.HumanService.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    CustomerService customerService;
    EmployeeService employeeService;

    @GetMapping("/get-customer/{customerId}")
    APIResponse<CustomerResponse> getCustomerById(@PathVariable("customerId") String customerId){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getById(customerId))
                .message("Successfully get user by follow id!")
                .build();
    }


    @GetMapping("/get-customers/list")
    @PreAuthorize("hasRole('ADMIN')")
    APIResponse<List<CustomerResponse>> getAllCustomers(){
        return APIResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAll())
                .message("Successfully get all customers!")
                .build();
    }

    //Get all sort by pages
    @GetMapping("/get-customers/list/{page}/{size}")
    APIResponse<Page<CustomerResponse>> getAllCustomers(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ){
        return APIResponse.<
                        Page<CustomerResponse>
                        >builder()
                .result(customerService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    //Get all sort by pages
    @GetMapping("/get-employees/list/{page}/{size}")
    APIResponse<Page<EmployeeResponse>> getAllEmployees(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ){
        return APIResponse.<
                        Page<EmployeeResponse>
                        >builder()
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


    @GetMapping("/get-employees/list")
    APIResponse<List<EmployeeResponse>> getAllEmployees(){
        return APIResponse.<List<EmployeeResponse>>builder()
                .result(employeeService.getAll())
                .message("Successfully get all customers!")
                .build();
    }
}
