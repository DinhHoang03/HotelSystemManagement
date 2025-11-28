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
public class RoomController {
    RoomService roomService;

    // 1. TẠO PHÒNG
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROOM_CREATE')")
    APIResponse<RoomResponse> create(@RequestBody RoomRequest request) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.create(request))
                .message("Create room successfully")
                .build();
    }

    // 2. CẬP NHẬT PHÒNG (API MỚI)
    // Dùng để: Đổi loại phòng, Đổi tầng, hoặc Tạp vụ cập nhật đã dọn xong (isClean = true)
    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ROOM_CREATE')") // Hoặc tạo quyền riêng 'ROOM_UPDATE' nếu muốn
    APIResponse<RoomResponse> update(@PathVariable("roomId") Long roomId, @RequestBody RoomRequest request) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.update(roomId, request))
                .message("Update room successfully")
                .build();
    }

    // 3. XEM CHI TIẾT 1 PHÒNG (API MỚI)
    @GetMapping("/info/{roomId}")
    @PreAuthorize("hasAuthority('ROOM_VIEW')")
    APIResponse<RoomResponse> getRoomById(@PathVariable("roomId") Long roomId) {
        return APIResponse.<RoomResponse>builder()
                .result(roomService.getById(roomId))
                .message("Get room detail successfully")
                .build();
    }

    // 4. DANH SÁCH PHÒNG (Phân trang)
    @GetMapping("/list")
    // Public hoặc ROOM_VIEW đều được
    APIResponse<Page<RoomResponse>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<RoomResponse>>builder()
                .result(roomService.getAll(page, size))
                .message("Successfully get all rooms!")
                .build();
    }

    // 5. XÓA PHÒNG
    @DeleteMapping("/del/{roomId}")
    @PreAuthorize("hasAuthority('ROOM_DELETE')")
    APIResponse<?> delete(@PathVariable("roomId") Long roomId) {
        roomService.delete(roomId);
        return APIResponse.builder()
                .message("Delete room " + roomId + " successfully")
                .build();
    }

    @GetMapping("/search")
    APIResponse<Page<RoomResponse>> searchByRoomType(
            @RequestParam String typeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return APIResponse.<Page<RoomResponse>>builder()
                .result(roomService.searchByRoomTypeName(typeName, page, size))
                .message("Search rooms successfully with keyword: " + typeName)
                .build();
    }
}