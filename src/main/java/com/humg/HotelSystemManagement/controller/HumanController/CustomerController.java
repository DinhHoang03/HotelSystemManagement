package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.customer.CustomerResponse;
import com.humg.HotelSystemManagement.service.HumanService.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    APIResponse<CustomerResponse> updateCustomer(@PathVariable("customerId")Long customerId,@Valid @RequestBody CustomerUpdateRequest request){
        return APIResponse.<CustomerResponse>builder()
                .result(customerService.updateById(customerId, request))
                .message("Update customer information successfully")
                .build();
    }

    @DeleteMapping("/user/del/{customerId}")
    APIResponse<String> deleteCustomer(@PathVariable("customerId") Long customerId){
        customerService.deleteById(customerId);
        return APIResponse.<String>builder()
                .message("Delete customer number id " + customerId + " successfully!")
                .build();
    }
}
