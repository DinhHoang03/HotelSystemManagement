package com.humg.HotelSystemManagement.modules.customer_service.resources.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardMetric {
    private long value;          // Giá trị thực (VD: 124 nhân viên)
    private double growth;       // % tăng trưởng (VD: 12.5)
    private String period;       // Mô tả kỳ so sánh (VD: "vs last month", "vs yesterday")
}