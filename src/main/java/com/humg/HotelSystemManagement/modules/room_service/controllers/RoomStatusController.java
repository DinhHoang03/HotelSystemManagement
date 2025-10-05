package com.humg.HotelSystemManagement.modules.room_service.controllers;

import com.humg.HotelSystemManagement.modules.room_service.resources.requests.RoomStatusRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomStatusResponse;
import com.humg.HotelSystemManagement.modules.room_service.services.RoomStatusService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room-service-status")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomStatusController {
    RoomStatusService roomStatusService;

    @PostMapping("/create")
    APIResponse<RoomStatusResponse> create(@RequestBody RoomStatusRequest request){
        return APIResponse.<RoomStatusResponse>builder()
                .result(roomStatusService.create(request))
                .message("Create permission successfully")
                .build();
    }

    @GetMapping("/get-all/list/{page}/{size}")
    APIResponse<Page<RoomStatusResponse>> getAllCustomers(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ){
        return APIResponse.<Page<RoomStatusResponse>>builder()
                .result(roomStatusService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    @DeleteMapping("/del/{roomStatus}")
    APIResponse delete(@RequestParam("roomStatus") Long id){
        roomStatusService.delete(id);

        return APIResponse.builder()
                .message("Delete room status:  " + id + " successfully")
                .build();
    }
}
