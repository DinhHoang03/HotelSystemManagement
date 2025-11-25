package com.humg.HotelSystemManagement.modules.room_service.controllers;

import com.humg.HotelSystemManagement.modules.room_service.resources.requests.RoomRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomResponse;
import com.humg.HotelSystemManagement.modules.room_service.services.RoomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class RoomController {
    RoomService roomService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROOM_CREATE')") // <--- Chỉ Quản lý/Admin
    APIResponse<RoomResponse> create(@RequestBody RoomRequest request) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.create(request))
                .message("Create room successfully")
                .build();
    }

    @GetMapping("/list/")
    @PreAuthorize("hasAuthority('ROOM_VIEW')") // <--- Ai cũng xem được (trừ người ngoài chưa login)
    APIResponse<Page<RoomResponse>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<RoomResponse>>builder()
                .result(roomService.getAll(page, size))
                .message("Successfully get all rooms!")
                .build();
    }

    @DeleteMapping("/del/{roomId}")
    @PreAuthorize("hasAuthority('ROOM_DELETE')") // <--- Chỉ Admin
    APIResponse<?> delete(@PathVariable("roomId") Long roomId) {
        roomService.delete(roomId);
        return APIResponse.builder()
                .message("Delete room " + roomId + " successfully")
                .build();
    }
}