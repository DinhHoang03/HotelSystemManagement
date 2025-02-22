package com.humg.HotelSystemManagement.controller;

import com.humg.HotelSystemManagement.dto.request.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.CustomerResponse;
import com.humg.HotelSystemManagement.entity.booking.Customer;
import com.humg.HotelSystemManagement.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    @Autowired
    CustomerService customerService;


    @PostMapping("/users")
    APIResponse<Customer> createCustomer(@Valid @RequestBody CustomerCreationRequest request){
        return APIResponse.<Customer>builder()
                .result(customerService.createCustomer(request))
                .message("The customer account is successfully created!")
                .build();
    }

    @GetMapping("/user/{customerId}")
    APIResponse<CustomerResponse> getCustomerById(@PathVariable("customerId") Long customerId){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.findUserById(customerId))
                .message("Successfully get user by follow id!")
                .build();
    }

    /**
    @GetMapping("/list")
    APIResponse<List<CustomerResponse>> getAllCustomers(){
        return APIResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAllUSers())
                .message("Successfully get user by follow id!")
                .build();
    }
    */

    @GetMapping("/list")
    List<Customer> getAllCustomers(){
        return customerService.getAllUSers();
    }
}
