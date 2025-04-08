package com.humg.HotelSystemManagement.controller.HotelController.room;

import com.humg.HotelSystemManagement.dto.request.room.RoomRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.dto.response.room.RoomResponse;
import com.humg.HotelSystemManagement.service.HotelService.hotel.RoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class RoomController {
    RoomService roomService;

    @PostMapping("/create")
    APIResponse<RoomResponse> create(@RequestBody RoomRequest request) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.create(request))
                .message("Create room successfully")
                .build();
    }

    @GetMapping("/get-all/list/{page}/{size}")
    APIResponse<Page<RoomResponse>> getAllRooms(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    ) {
        return APIResponse.<Page<RoomResponse>>builder()
                .result(roomService.getAll(page, size))
                .message("Successfully get all rooms!")
                .build();
    }

    /*
    @GetMapping("/get-by-number/{roomNumber}")
    APIResponse<RoomResponse> getByRoomNumber(
            @PathVariable("roomNumber") String roomNumber
    ) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.getByRoomNumber(roomNumber))
                .message("Successfully get room by number!")
                .build();
    }
    **/

    @DeleteMapping("/del/{roomId}")
    APIResponse delete(@PathVariable("roomId") Long roomId) {
        roomService.delete(roomId);

        return APIResponse.builder()
                .message("Delete room " + roomId + " successfully")
                .build();
    }
}