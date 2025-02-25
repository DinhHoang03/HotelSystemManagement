package com.humg.HotelSystemManagement.controller;

import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.cleaner.CleanerResponse;
import com.humg.HotelSystemManagement.entity.employees.Cleaner;
import com.humg.HotelSystemManagement.service.CleanerService;
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

    @PostMapping("/create-account")
    APIResponse<Cleaner> createCleaner(@Valid @RequestBody CleanerCreationRequest request){
        return APIResponse.<Cleaner>builder()
                .result(cleanerService.createCleaner(request))
                .message("The cleaner account is successfully created!")
                .build();
    }

    @PutMapping("/update/{cleanerId}")
    APIResponse<CleanerResponse> updateCleaner(@PathVariable("cleanerId") Long cleanerId,@Valid @RequestBody CleanerUpdateRequest request){
        return APIResponse.<CleanerResponse>builder()
                .result(cleanerService.updateCleaner(cleanerId, request))
                .message("Successfully updated user!")
                .build();
    }

    @DeleteMapping("/del/{cleanerId}")
    APIResponse<String> deleteCleaner(@PathVariable("cleanerId") Long cleanerId){
        return APIResponse.<String>builder()
                .message("Delete cleaner number id " + cleanerId + " successfully!")
                .build();
    }
}
