package com.humg.HotelSystemManagement.controller.HotelController.room;

import com.humg.HotelSystemManagement.dto.request.roomType.RoomTypeRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.roomType.RoomTypeResponse;
import com.humg.HotelSystemManagement.service.SystemServices.hotel.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room-type")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomTypeController {
    RoomTypeService roomTypeService;

    @PostMapping("/create")
    APIResponse<RoomTypeResponse> create(@RequestBody RoomTypeRequest request){
        return APIResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.create(request))
                .message("Create permission successfully")
                .build();
    }

    @GetMapping("/get-all/list/{page}/{size}")
    APIResponse<Page<RoomTypeResponse>> getAllCustomers(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ){
        return APIResponse.<Page<RoomTypeResponse>>builder()
                .result(roomTypeService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    @DeleteMapping("/del/{serviceName}")
    APIResponse delete(@RequestParam("serviceName") Long id){
        roomTypeService.delete(id);

        return APIResponse.builder()
                .message("Delete permission " + id + " successfully")
                .build();
    }
}
