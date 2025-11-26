package com.humg.HotelSystemManagement.modules.room_service.controllers;

import com.humg.HotelSystemManagement.modules.room_service.resources.requests.RoomTypeRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.room_service.resources.responses.RoomTypeResponse;
import com.humg.HotelSystemManagement.modules.room_service.services.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomTypeController {
    RoomTypeService roomTypeService;

    // 1. TẠO LOẠI PHÒNG
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROOM_CREATE')")
    APIResponse<RoomTypeResponse> create(@RequestBody RoomTypeRequest request){
        return APIResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.create(request))
                .message("Create room type successfully")
                .build();
    }

    // 2. CẬP NHẬT LOẠI PHÒNG (API MỚI) - Quan trọng để đổi giá tiền
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ROOM_CREATE')")
    APIResponse<RoomTypeResponse> update(@PathVariable("id") Long id, @RequestBody RoomTypeRequest request){
        return APIResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.update(id, request))
                .message("Update room type successfully")
                .build();
    }

    // 3. XEM CHI TIẾT (API MỚI)
    @GetMapping("/info/{id}")
    // Public để khách xem chi tiết loại phòng (ảnh, tiện ích) trước khi đặt
    APIResponse<RoomTypeResponse> getById(@PathVariable("id") Long id){
        return APIResponse.<RoomTypeResponse>builder()
                .result(roomTypeService.getById(id))
                .message("Get room type detail successfully")
                .build();
    }

    // 4. DANH SÁCH
    @GetMapping("/list") // Đổi path thành /list cho ngắn gọn
    // Public
    APIResponse<Page<RoomTypeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<Page<RoomTypeResponse>>builder()
                .result(roomTypeService.getAll(page, size))
                .message("Successfully get all room types!")
                .build();
    }

    // 5. XÓA
    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('ROOM_DELETE')")
    APIResponse<String> delete(@PathVariable("id") Long id){
        roomTypeService.delete(id);
        return APIResponse.<String>builder()
                .message("Delete room type " + id + " successfully")
                .build();
    }
}