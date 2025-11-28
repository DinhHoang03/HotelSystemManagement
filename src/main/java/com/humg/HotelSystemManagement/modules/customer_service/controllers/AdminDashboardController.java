package com.humg.HotelSystemManagement.modules.customer_service.controllers;

import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.DashboardMetric;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.RecentBookingResponse;
import com.humg.HotelSystemManagement.modules.customer_service.services.AdminDashboardStatService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')") // Đảm bảo chỉ Admin mới gọi được
public class AdminDashboardController {

    AdminDashboardStatService dashboardStatService;

    /**
     * API 1: Lấy tổng hợp 4 thẻ thống kê trên cùng (Top Cards)
     * URL: GET /api/v1/admin/dashboard/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        // Bây giờ gọi các hàm getMetric... thay vì getCount...
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .accounts(dashboardStatService.getAccountMetric())
                .rooms(dashboardStatService.getRoomMetric())
                .bookings(dashboardStatService.getBookingMetric())
                .revenue(dashboardStatService.getRevenueMetric())
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * API 2: Dữ liệu biểu đồ doanh thu theo tháng
     * URL: GET /api/v1/admin/dashboard/chart/revenue?year=2025
     * Nếu không truyền year, mặc định lấy năm hiện tại
     */
    @GetMapping("/chart/revenue")
    public ResponseEntity<Map<String, Long>> getMonthlyRevenueChart(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(dashboardStatService.getMonthlyRevenue(targetYear));
    }

    /**
     * API 3: Dữ liệu biểu đồ tỉ lệ lấp đầy phòng trong tuần
     * URL: GET /api/v1/admin/dashboard/chart/occupancy
     */
    @GetMapping("/chart/occupancy")
    public ResponseEntity<Map<String, Double>> getWeeklyOccupancyChart() {
        return ResponseEntity.ok(dashboardStatService.getWeeklyOccupancy());
    }

    /**
     * API 4: Danh sách 5 đơn đặt phòng gần đây nhất
     * URL: GET /api/v1/admin/dashboard/recent-bookings
     */
    @GetMapping("/recent-bookings")
    public ResponseEntity<List<RecentBookingResponse>> getRecentBookings() {
        return ResponseEntity.ok(dashboardStatService.getRecentBookings());
    }

    // =============================================================
    // Inner DTO Class cho API Summary (Để gọn code, có thể tách ra file riêng)
    // =============================================================
    @Data
    @Builder
    public static class DashboardSummaryResponse {
        DashboardMetric accounts; // Total Employees/Accounts
        DashboardMetric rooms;    // Total Rooms
        DashboardMetric bookings; // Today's Bookings
        DashboardMetric revenue;  // Today's Revenue
    }
}