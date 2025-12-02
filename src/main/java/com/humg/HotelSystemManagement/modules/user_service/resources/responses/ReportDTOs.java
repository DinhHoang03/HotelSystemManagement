package com.humg.HotelSystemManagement.modules.user_service.resources.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ReportDTOs {
    // 1. Response to nhất trả về cho API
    @Data
    @Builder
    public static class ReportDashboardResponse {
        ReportSummaryCard summary;
        RevenueChart revenueAnalysis;
        List<RoomTypeStat> bookingsByRoom;
        List<AgeGroupStat> customerDemographics;
        List<TopServiceStat> topServices;
    }

    // 2. Các thành phần con bên trong
    @Data
    @Builder
    public static class ReportSummaryCard {
        StatItem totalRevenue;
        StatItem avgOccupancy;
        StatItem totalBookings;
        StatItem newCustomers;
    }

    @Data
    @Builder
    public static class StatItem {
        Object value; // Double hoặc Long
        double percentChange; // Ví dụ: 12.5%
        boolean isIncrease;   // true: tăng (xanh), false: giảm (đỏ)
    }

    @Data
    @Builder
    public static class RevenueChart {
        List<String> labels;      // ["Jan", "Feb"...]
        List<Double> revenueData; // [1000, 2000...]
        List<Double> expenseData; // [500, 800...]
    }

    @Data
    @Builder
    public static class RoomTypeStat {
        String name;       // "Luxury Suite"
        long value;        // 120 đơn
        double percentage; // 35.5%
    }

    @Data
    @Builder
    public static class AgeGroupStat {
        String range; // "18-25"
        long count;   // 50 người
    }

    @Data
    @Builder
    public static class TopServiceStat {
        int rank;            // 1
        String name;         // "Spa"
        long bookingsCount;  // 100
        double totalRevenue; // 5000$
        double growth;       // 10%
    }
}
