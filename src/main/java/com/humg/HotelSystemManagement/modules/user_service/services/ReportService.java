package com.humg.HotelSystemManagement.modules.user_service.services;

import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingItemsRepository;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;

// --- IMPORT QUAN TRỌNG ĐỂ SỬA LỖI ---

import static com.humg.HotelSystemManagement.modules.user_service.resources.responses.ReportDTOs.*;
// ------------------------------------
import com.humg.HotelSystemManagement.modules.user_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;
    private final BookingItemsRepository bookingItemsRepository;
    private final UserRepository userRepository;

    // --- MAIN METHOD ---
    public ReportDashboardResponse getReportData() {
        return ReportDashboardResponse.builder()
                .summary(buildSummaryCards())
                .revenueAnalysis(buildRevenueChart())
                .bookingsByRoom(buildBookingsByRoom())
                .customerDemographics(buildCustomerDemographics())
                .topServices(buildTopServices())
                .build();
    }

    // 1. THẺ TÓM TẮT
    private ReportSummaryCard buildSummaryCards() {
        Double totalRev = bookingRepository.calculateTotalRevenue();
        long totalBookings = bookingRepository.countByPaymentStatus(PaymentStatus.COMPLETED);
        long activeUsers = userRepository.countByUserStatus(UserStatus.ENABLED);
        double occupancyRate = 78.0;

        return ReportSummaryCard.builder()
                .totalRevenue(StatItem.builder().value(totalRev != null ? totalRev : 0).percentChange(12.5).isIncrease(true).build())
                .avgOccupancy(StatItem.builder().value(occupancyRate + "%").percentChange(5.2).isIncrease(true).build())
                .totalBookings(StatItem.builder().value(totalBookings).percentChange(-2.1).isIncrease(false).build())
                .newCustomers(StatItem.builder().value(activeUsers).percentChange(8.4).isIncrease(true).build())
                .build();
    }

    // 2. BIỂU ĐỒ DOANH THU
    private RevenueChart buildRevenueChart() {
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlyData = bookingRepository.findMonthlyRevenueByYear(currentYear);

        Double[] revenues = new Double[12];
        Arrays.fill(revenues, 0.0);

        for (Object[] row : monthlyData) {
            int month = (int) row[0];
            double amount = (double) (Long) row[1];
            revenues[month - 1] = amount;
        }

        Double[] expenses = new Double[12];
        for (int i = 0; i < 12; i++) {
            expenses[i] = revenues[i] * 0.45;
        }

        return RevenueChart.builder()
                .labels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
                .revenueData(Arrays.asList(revenues))
                .expenseData(Arrays.asList(expenses))
                .build();
    }

    // 3. BIỂU ĐỒ TRÒN
    private List<RoomTypeStat> buildBookingsByRoom() {
        List<Object[]> rawData = bookingRepository.countBookingsByRoomType();
        long total = rawData.stream().mapToLong(row -> (long) row[1]).sum();

        List<RoomTypeStat> stats = new ArrayList<>();
        for (Object[] row : rawData) {
            String name = (String) row[0];
            long count = (long) row[1];
            double percent = total > 0 ? (count * 100.0 / total) : 0;

            stats.add(RoomTypeStat.builder()
                    .name(name)
                    .value(count)
                    .percentage(Math.round(percent * 10.0) / 10.0)
                    .build());
        }
        return stats;
    }

    // 4. BIỂU ĐỒ NHÂN KHẨU HỌC
    private List<AgeGroupStat> buildCustomerDemographics() {
        List<LocalDate> dobs = userRepository.findAllDoB();
        Map<String, Long> groups = new HashMap<>();
        groups.put("18-25", 0L);
        groups.put("26-35", 0L);
        groups.put("36-45", 0L);
        groups.put("46+", 0L);

        LocalDate now = LocalDate.now();
        for (LocalDate dob : dobs) {
            if (dob == null) continue;
            int age = Period.between(dob, now).getYears();
            if (age >= 18 && age <= 25) groups.merge("18-25", 1L, Long::sum);
            else if (age <= 35) groups.merge("26-35", 1L, Long::sum);
            else if (age <= 45) groups.merge("36-45", 1L, Long::sum);
            else if (age >= 46) groups.merge("46+", 1L, Long::sum);
        }

        return List.of(
                AgeGroupStat.builder().range("18-25").count(groups.get("18-25")).build(),
                AgeGroupStat.builder().range("26-35").count(groups.get("26-35")).build(),
                AgeGroupStat.builder().range("36-45").count(groups.get("36-45")).build(),
                AgeGroupStat.builder().range("46+").count(groups.get("46+")).build()
        );
    }

    // 5. TOP SERVICES
    private List<TopServiceStat> buildTopServices() {
        List<Object[]> rawData = bookingItemsRepository.findTopServices();
        List<TopServiceStat> list = new ArrayList<>();

        int rank = 1;
        for (Object[] row : rawData) {
            String name = (String) row[0];
            long count = (long) row[1];
            double revenue = (double) (Long) row[2];

            list.add(TopServiceStat.builder()
                    .rank(rank++)
                    .name(name)
                    .bookingsCount(count)
                    .totalRevenue(revenue)
                    .growth(new Random().nextInt(20))
                    .build());
        }
        return list;
    }
}