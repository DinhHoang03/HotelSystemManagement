package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.cleaner.CleanerResponse;
import com.humg.HotelSystemManagement.service.HumanService.CleanerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/cleaner")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanerController {
    CleanerService cleanerService;

    @PostMapping("/register")
    APIResponse<CleanerResponse> createCleaner(@Valid @RequestBody CleanerCreationRequest request){
        return APIResponse.<CleanerResponse>builder()
                .result(cleanerService.create(request))
                .message("The cleaner account is successfully created!")
                .build();
    }

    @PutMapping("/update/{cleanerId}")
    APIResponse<CleanerResponse> updateCleaner(@PathVariable("cleanerId") Long cleanerId,@Valid @RequestBody CleanerUpdateRequest request){
        return APIResponse.<CleanerResponse>builder()
                .result(cleanerService.updateById(cleanerId, request))
                .message("Successfully updated user!")
                .build();
    }

    @DeleteMapping("/del/{cleanerId}")
    APIResponse<String> deleteCleaner(@PathVariable("cleanerId") Long cleanerId){
        cleanerService.deleteById(cleanerId);
        return APIResponse.<String>builder()
                .message("Delete cleaner number id " + cleanerId + " successfully!")
                .build();
    }
}
