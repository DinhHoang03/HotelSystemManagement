package com.humg.HotelSystemManagement.modules.user_service.services;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRoomRepository;
import com.humg.HotelSystemManagement.modules.user_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.DashboardMetric;
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.RecentBookingResponse;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardStatService {

    UserRepository userRepository;
    RoomRepository roomRepository;
    BookingRoomRepository bookingRoomRepository;
    PaymentBillRepository paymentBillRepository;
    BookingRepository bookingRepository;

    // =================================================================
    // PHẦN 1: TOP CARDS (4 Ô thống kê trên cùng)
    // =================================================================

    private double calculateGrowth(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        double growth = ((double) (current - previous) / previous) * 100;
        return Math.round(growth * 100.0) / 100.0; // Làm tròn 2 số thập phân
    }

    // 1. Total Accounts (So sánh với tháng trước)
    public DashboardMetric getAccountMetric() {
        long currentTotal = userRepository.count();

        // Giả sử logic: Lấy tổng user tạo trong tháng này
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        // Cần thêm hàm countByCreatedDateAfter trong Repo. Nếu chưa có thì tạm tính growth = 0
        long newThisMonth = 0;
        // long newThisMonth = userRepository.countByCreatedDateAfter(firstDayOfMonth.atStartOfDay());

        long lastMonthTotal = currentTotal - newThisMonth;

        return DashboardMetric.builder()
                .value(currentTotal)
                .growth(calculateGrowth(currentTotal, lastMonthTotal)) // Logic tạm
                .period("vs last month")
                .build();
    }

    // 2. Total Rooms (Inventory ít thay đổi, thường so sánh tháng)
    public DashboardMetric getRoomMetric() {
        long currentTotal = roomRepository.count();
        // Room ít khi thêm mới hàng ngày, nên giả sử growth = 0 hoặc random cho đẹp nếu là demo
        return DashboardMetric.builder()
                .value(currentTotal)
                .growth(0.0)
                .period("vs last month")
                .build();
    }

    // 3. Today's Bookings (So sánh Hôm nay vs Hôm qua)
    public DashboardMetric getBookingMetric() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = bookingRepository.countByBookingDate(today);
        long yesterdayCount = bookingRepository.countByBookingDate(yesterday);

        return DashboardMetric.builder()
                .value(todayCount)
                .growth(calculateGrowth(todayCount, yesterdayCount))
                .period("vs yesterday")
                .build();
    }

    // 4. Today's Revenue (So sánh Hôm nay vs Hôm qua)
    public DashboardMetric getRevenueMetric() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Long todayRev = paymentBillRepository.getTodayRenevue(today);
        Long yesterdayRev = paymentBillRepository.getTodayRenevue(yesterday);

        long current = todayRev == null ? 0L : todayRev;
        long previous = yesterdayRev == null ? 0L : yesterdayRev;

        return DashboardMetric.builder()
                .value(current)
                .growth(calculateGrowth(current, previous))
                .period("vs yesterday")
                .build();
    }

    // =================================================================
    // PHẦN 2: CHARTS (Biểu đồ ở giữa)
    // =================================================================

    // Revenue Overview (Line Chart)
    public Map<String, Long> getMonthlyRevenue(int year) {
        // Mặc định lấy từ tháng 1 đến tháng 12 của năm truyền vào
        List<Object[]> results = bookingRepository.findMonthlyRevenue(year, 1, 12);

        // Dùng LinkedHashMap để giữ thứ tự tháng Jan -> Dec
        Map<String, Long> monthlyRevenue = new LinkedHashMap<>();

        // Khởi tạo 12 tháng bằng 0
        for (int i = 1; i <= 12; i++) {
            String monthName = java.time.Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyRevenue.put(monthName, 0L);
        }

        // Fill dữ liệu thật
        for (Object[] row : results) {
            int month = (int) row[0];
            Long total = (Long) row[1];
            String monthName = java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyRevenue.put(monthName, total);
        }
        return monthlyRevenue;
    }

    // Room Occupancy (Bar Chart)
    public Map<String, Double> getWeeklyOccupancy() {
        // LinkedHashMap để giữ thứ tự Mon -> Sun
        Map<String, Double> occupancyRates = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        long totalRooms = roomRepository.countTotalRooms();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            if (totalRooms == 0) {
                occupancyRates.put(dayName, 0.0);
                continue;
            }

            List<BookingRoom> activeBookings = bookingRoomRepository.findActiveBookingsOnDate(date);
            long occupiedRooms = activeBookings.stream()
                    .mapToLong(br -> br.getRooms().size())
                    .sum();

            double rate = ((double) occupiedRooms / totalRooms) * 100;
            occupancyRates.put(dayName, Math.round(rate * 100.0) / 100.0);
        }
        return occupancyRates;
    }

    // =================================================================
    // PHẦN 3: BOTTOM TABLE (Recent Bookings)
    // =================================================================

    public List<RecentBookingResponse> getRecentBookings() {
        // Lấy 5 đơn mới nhất theo ngày đặt
        List<Booking> recentBookings = bookingRepository.findTop5ByOrderByBookingDateDesc();

        return recentBookings.stream().map(booking -> {
            String roomName = "Unknown";
            LocalDate checkInDate = booking.getBookingDate();

            if (booking.getBookingRooms() != null && !booking.getBookingRooms().isEmpty()) {
                BookingRoom firstBookingRoom = booking.getBookingRooms().get(0);
                checkInDate = firstBookingRoom.getCheckInDate();

                if (firstBookingRoom.getRooms() != null && !firstBookingRoom.getRooms().isEmpty()) {
                    // --- SỬA TẠI ĐÂY ---
                    // Thay .getName() thành .getRoomNumber()
                    roomName = firstBookingRoom.getRooms().get(0).getRoomNumber();
                    // -------------------
                }

                if (booking.getBookingRooms().size() > 1) {
                    roomName += " (+" + (booking.getBookingRooms().size() - 1) + " others)";
                }
            }

            return RecentBookingResponse.builder()
                    .customerName(booking.getUser().getName())
                    .roomName(roomName)
                    .checkInDate(checkInDate)
                    .status(booking.getBookingStatus().toString())
                    .amount(Double.valueOf(booking.getGrandTotal()))
                    .build();
        }).collect(Collectors.toList());
    }
}