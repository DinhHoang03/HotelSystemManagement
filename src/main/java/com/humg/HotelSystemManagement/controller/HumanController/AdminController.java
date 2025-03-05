package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.customer.CustomerResponse;
import com.humg.HotelSystemManagement.dto.response.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.service.HumanService.CustomerService;
import com.humg.HotelSystemManagement.service.HumanService.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    CustomerService customerService;
    EmployeeService employeeService;

    @GetMapping("/get-customer/{customerId}")
    APIResponse<CustomerResponse> getCustomerById(@PathVariable("customerId") Long customerId){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.getById(customerId))
                .message("Successfully get user by follow id!")
                .build();
    }


    @GetMapping("/get-customers/list")
    APIResponse<List<CustomerResponse>> getAllCustomers(){
        return APIResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAll())
                .message("Successfully get all customers!")
                .build();
    }

    @GetMapping("/get-employee/{employeeId}")
    APIResponse<EmployeeResponse> getEmployeeById(@PathVariable("employeeId") Long employeeId){
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
