package com.humg.HotelSystemManagement.modules.user_service.controllers;

// Import DTO và Service từ đúng package
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.ReportDTOs.ReportDashboardResponse;
import com.humg.HotelSystemManagement.modules.user_service.services.ExcelExportService;
import com.humg.HotelSystemManagement.modules.user_service.services.ReportService;
import com.humg.HotelSystemManagement.utils.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ExcelExportService excelExportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    public APIResponse<ReportDashboardResponse> getDashboardStats() {
        return APIResponse.<ReportDashboardResponse>builder()
                .result(reportService.getReportData())
                .message("Get dashboard reports successfully")
                .build();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('SYSTEM_MANAGE')")
    public ResponseEntity<InputStreamResource> exportReport() throws IOException {

        ByteArrayInputStream in = excelExportService.exportDashboardData();

        String filename = "hotel_report_" + System.currentTimeMillis() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
