package com.hotel.humg.HotelSystemManagement.controller.HotelController.room;

import com.hotel.humg.HotelSystemManagement.dto.request.room.roomType.RoomTypeRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.APIResponse;
import com.hotel.humg.HotelSystemManagement.dto.response.room.roomType.RoomTypeResponse;
import com.hotel.humg.HotelSystemManagement.service.HotelService.hotel.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/type")
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

    @GetMapping("/get-all/list")
    APIResponse<Page<RoomTypeResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<Page<RoomTypeResponse>>builder()
                .result(roomTypeService.getAll(page, size))
                .message("Successfully get all customers!")
                .build();
    }

    @DeleteMapping("/del/{serviceName}")
    APIResponse<String> delete(@PathVariable("serviceName") Long id){
        roomTypeService.delete(id);

        return APIResponse.<String>builder()
                .message("Delete permission " + id + " successfully")
                .build();
    }
}
