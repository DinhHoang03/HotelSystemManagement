package com.humg.HotelSystemManagement.modules.customer_service.controllers;

import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.UserCreationRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.UserUpdateRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.customer_service.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    APIResponse<UserResponse> createCustomer(@Valid @RequestBody UserCreationRequest request){
        return APIResponse.<UserResponse>builder()
                .result(userService.create(request))
                .message("The customer account is successfully created!")
                .build();
    }

    @PutMapping("/update/{customerId}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    APIResponse<UserResponse> updateCustomer(@PathVariable("customerId")String customerId, @Valid @RequestBody UserUpdateRequest request){
        return APIResponse.<UserResponse>builder()
                .result(userService.update(customerId, request))
                .message("Update customer information successfully")
                .build();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE')")
    UserResponse getMyInfo() {
        return userService.getMyInfo();
    }

    @DeleteMapping("/user/del/{customerId}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    APIResponse<String> deleteCustomer(@PathVariable("customerId") String customerId){
        userService.delete(customerId);
        return APIResponse.<String>builder()
                .message("Delete customer number id " + customerId + " successfully!")
                .build();
    }
}
