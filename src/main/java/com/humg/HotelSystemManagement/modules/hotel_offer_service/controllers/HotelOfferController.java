package com.humg.HotelSystemManagement.modules.hotel_offer_service.controllers;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests.HotelOfferRequest;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses.HotelOfferResponse;
import com.humg.HotelSystemManagement.modules.hotel_offer_service.services.HotelOfferService;
import com.humg.HotelSystemManagement.utils.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/offers") // Đổi path thành /api/v1/offers cho chuẩn REST
@RequiredArgsConstructor
public class HotelOfferController {
    private final HotelOfferService hotelService;

    // 1. TẠO DỊCH VỤ (KÈM ẢNH)
    // Sử dụng consumes = MULTIPART_FORM_DATA_VALUE để nhận file
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('OFFER_CREATE')")
    public APIResponse<HotelOfferResponse> create(
            @RequestPart("data") HotelOfferRequest request, // Phần thông tin (JSON string)
            @RequestPart(value = "image", required = false) MultipartFile image // Phần ảnh (File)
    ) {
        return APIResponse.<HotelOfferResponse>builder()
                .result(hotelService.create(request, image))
                .message("Tạo dịch vụ thành công")
                .build();
    }

    // 2. LẤY TẤT CẢ (Phân trang)
    @GetMapping("/list")
    public APIResponse<Page<HotelOfferResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return APIResponse.<Page<HotelOfferResponse>>builder()
                .result(hotelService.getAll(page, size))
                .message("Lấy danh sách dịch vụ thành công")
                .build();
    }

    // 3. LẤY THEO DANH MỤC (Để hiển thị Menu riêng: FOOD, SPA...)
    // API: /api/v1/offers/category/FOOD
    @GetMapping("/category/{categoryName}")
    public APIResponse<List<HotelOfferResponse>> getByCategory(@PathVariable String categoryName) {
        return APIResponse.<List<HotelOfferResponse>>builder()
                .result(hotelService.getByCategory(categoryName))
                .message("Lấy danh mục thành công")
                .build();
    }

    // 4. XÓA DỊCH VỤ
    @DeleteMapping("/del/{id}")
    @PreAuthorize("hasAuthority('OFFER_DELETE')")
    public APIResponse<String> delete(@PathVariable String id){
        hotelService.delete(id);
        return APIResponse.<String>builder()
                .message("Xóa dịch vụ thành công")
                .build();
    }

    // 5. LẤY CHI TIẾT THEO ID
    @GetMapping("/{id}")
    public APIResponse<HotelOfferResponse> getById(@PathVariable String id) {
        return APIResponse.<HotelOfferResponse>builder()
                .result(hotelService.getById(id))
                .message("Lấy thông tin dịch vụ thành công")
                .build();
    }

    // 6. CẬP NHẬT DỊCH VỤ
    // Method PUT, Consumes Multipart để nhận file ảnh mới (nếu có)
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('OFFER_UPDATE')") // Nhớ thêm quyền này trong DB nếu chưa có
    public APIResponse<HotelOfferResponse> update(
            @PathVariable String id,
            @RequestPart("data") HotelOfferRequest request, // Thông tin sửa đổi
            @RequestPart(value = "image", required = false) MultipartFile image // Ảnh mới (không bắt buộc)
    ) {
        return APIResponse.<HotelOfferResponse>builder()
                .result(hotelService.update(id, request, image))
                .message("Cập nhật dịch vụ thành công")
                .build();
    }
}