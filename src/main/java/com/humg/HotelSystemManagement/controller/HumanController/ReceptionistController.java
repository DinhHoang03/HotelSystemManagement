package com.humg.HotelSystemManagement.controller.HumanController;

import com.humg.HotelSystemManagement.dto.request.receptionist.ReceptionistCreationRequest;
import com.humg.HotelSystemManagement.dto.request.receptionist.ReceptionistUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.receptionist.ReceptionistResponse;
import com.humg.HotelSystemManagement.service.HumanService.ReceptionistService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/receptionist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReceptionistController {
    ReceptionistService receptionistService;

    @PostMapping("/register")
    public APIResponse<ReceptionistResponse> createReceptionist(@Valid @RequestBody ReceptionistCreationRequest request){
        return APIResponse.<ReceptionistResponse>builder()
                .result(receptionistService.create(request))
                .message("The receptionist account is successfully created!")
                .build();
    }

    @PutMapping("/update/{receptionistId}")
    public APIResponse<ReceptionistResponse> updateReceptionist(@PathVariable("receptionistId") Long id, @Valid @RequestBody ReceptionistUpdateRequest request){
        return APIResponse.<ReceptionistResponse>builder()
                .result(receptionistService.updateById(id, request))
                .message("Successfully updated user!")
                .build();
    }

    @DeleteMapping("/del/{receptionistId}")
    public APIResponse<String> deleteReceptionist(@PathVariable("receptionistID") Long id){
        receptionistService.deleteById(id);
        return APIResponse.<String>builder()
                .message("Delete receptionist number id " + id + " successfully!")
                .build();
    }
}
