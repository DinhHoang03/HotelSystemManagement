package com.humg.HotelSystemManagement.modules.customer_service.controllers;

import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.CustomerCreationRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.CustomerResponse;
import com.humg.HotelSystemManagement.modules.customer_service.services.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    CustomerService customerService;

    @PostMapping("/register")
    APIResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest request){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.create(request))
                .message("The customer account is successfully created!")
                .build();
    }

    @PutMapping("/update/{customerId}")
    APIResponse<CustomerResponse> updateCustomer(@PathVariable("customerId")String customerId,@Valid @RequestBody CustomerUpdateRequest request){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.update(customerId, request))
                .message("Update customer information successfully")
                .build();
    }

    @GetMapping("/profile")
    CustomerResponse getMyInfo() {
        return customerService.getMyInfo();
    }

    @DeleteMapping("/user/del/{customerId}")
    APIResponse<String> deleteCustomer(@PathVariable("customerId") String customerId){
        customerService.delete(customerId);
        return APIResponse.<String>builder()
                .message("Delete customer number id " + customerId + " successfully!")
                .build();
    }
}
