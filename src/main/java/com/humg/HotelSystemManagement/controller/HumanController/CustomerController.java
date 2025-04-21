package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.humanEntity.customer.CustomerResponse;
import com.humg.HotelSystemManagement.service.HumanService.CustomerService;
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
