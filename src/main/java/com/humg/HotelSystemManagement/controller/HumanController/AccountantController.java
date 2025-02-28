package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.accountant.AccountantCreationRequest;
import com.humg.HotelSystemManagement.dto.request.accountant.AccountantUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.accountant.AccountantResponse;
import com.humg.HotelSystemManagement.service.HumanService.AccountantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/accountant")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountantController {

    AccountantService accountantService;

    @PostMapping("/register")
    APIResponse<AccountantResponse> createAccountant(@Valid @RequestBody AccountantCreationRequest request){
        return APIResponse.<AccountantResponse>builder()
                .result(accountantService.create(request))
                .message("This accountant account is successfully created")
                .build();
    }

    @PutMapping("/update/{accountantId}")
    APIResponse<AccountantResponse> updateAccountant(@PathVariable("accountantId") Long id, @Valid @RequestBody AccountantUpdateRequest request){
        return APIResponse.<AccountantResponse>builder()
                .result(accountantService.updateById(id, request))
                .message("Update accountant successfully!")
                .build();
    }

    @DeleteMapping("del/{accountantId}")
    APIResponse<String> deleteAccountant(@PathVariable("accountantId") Long id){
        accountantService.deleteById(id);
        return APIResponse.<String>builder()
                .message("Delete accountant number id " + id + " successfully!")
                .build();
    }
}
