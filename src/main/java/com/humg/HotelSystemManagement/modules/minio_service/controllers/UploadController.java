package com.humg.HotelSystemManagement.modules.minio_service.controllers;

import com.humg.HotelSystemManagement.modules.minio_service.services.MinioService;
import com.humg.HotelSystemManagement.utils.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UploadController {

    private final MinioService minioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('FILES_UPLOAD')")
    public APIResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadFile(file);

        return APIResponse.<String>builder()
                .code(1000)
                .message("Upload thành công")
                .result(imageUrl)
                .build();
    }
}