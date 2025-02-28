package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.waiter.WaiterCreationRequest;
import com.humg.HotelSystemManagement.dto.request.waiter.WaiterUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.waiter.WaiterResponse;
import com.humg.HotelSystemManagement.service.HumanService.WaiterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/waiter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaiterController {
    WaiterService  waiterService;

    @PostMapping("/register")
    APIResponse<WaiterResponse> createWaiter(@Valid @RequestBody WaiterCreationRequest request){
        return APIResponse.<WaiterResponse>builder()
                .result(waiterService.create(request))
                .message("The waiter account is successfully created!")
                .build();
    }

    @PutMapping("/update/{waiterId}")
    APIResponse<WaiterResponse> updateWaiter(@PathVariable("waiterId") Long waiterId,@Valid @RequestBody WaiterUpdateRequest request){
        return APIResponse.<WaiterResponse>builder()
                .result(waiterService.updateById(waiterId, request))
                .message("Update waiter successfully!")
                .build();
    }

    @DeleteMapping("/del/{waiterId}")
    APIResponse<String> deleteWaiter(@PathVariable("waiterId") Long waiterId){
        waiterService.deleteById(waiterId);
        return APIResponse.<String>builder()
                .message("Delete waiter number id " + waiterId + " successfully!")
                .build();
    }
}
