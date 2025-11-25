package com.humg.HotelSystemManagement.modules.admin_service.services;

import com.humg.HotelSystemManagement.exceptions.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exceptions.exceptions.AppException;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRepository;
import com.humg.HotelSystemManagement.modules.booking_service.models.repositories.BookingRoomRepository;
import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.UserResponse;
import com.humg.HotelSystemManagement.modules.payment_service.models.repositories.PaymentBillRepository;
import com.humg.HotelSystemManagement.modules.customer_service.models.repositories.UserRepository;
import com.humg.HotelSystemManagement.modules.room_service.models.repositories.RoomRepository;
import com.humg.HotelSystemManagement.utils.enums.UserStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {

    UserRepository userRepository;
    RoomRepository roomRepository;
    BookingRoomRepository bookingRoomRepository;
    PaymentBillRepository paymentBillRepository;
    BookingRepository bookingRepository;

    public String enableUser(String id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(user.getUserStatus() == UserStatus.DISABLED){
            user.setUserStatus(UserStatus.ENABLED);
            userRepository.save(user);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return "User " + user.getName() + " has successfully " + user.getUserStatus().name();
    }

    public String disableUser(String id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(user.getUserStatus() == UserStatus.ENABLED){
            user.setUserStatus(UserStatus.DISABLED);
            userRepository.save(user);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return "User " + user.getName() + " has successfully " + user.getUserStatus().name();
    }

    public Long countCustomerByList() {
        return userRepository.count();
    }

    public Long countRoomByList() {
        return roomRepository.count();
    }

    public Long countBookingTodayByList(LocalDate now) {
        List<Booking> bookingToday = bookingRepository.getBookingsToday(now);

        return (long) bookingToday.size();
    }

    public Long getTodayRevenue(LocalDate now) {
        return paymentBillRepository.getTodayRenevue(now);
    }

    public Long totalCountUser() {

        return userRepository.count();
    }

    //Tomorrow refactor
    public Map<String, Long> getMonthlyRevenue(int year, int startMonth, int endMonth) {
        List<Object[]> results = bookingRepository.findMonthlyRevenue(year, startMonth, endMonth);
        Map<String, Long>  monthlyRevenue = new HashMap<>();

        //Khởi tạo dữ liệu cho các tháng
        for (int i = startMonth; i <= endMonth; i++) {
            monthlyRevenue.put(getMonthName(i), 0L);
        }

        //Ghi dữ liệu từ truy vấn
        for(Object[] row : results) {
            int month = (int) row[0];
            Long total = (Long) row[1];
            monthlyRevenue.put(getMonthName(month), total);
        }
        return monthlyRevenue;
    }

    public String getMonthName(int month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }

    public Map<String, Double> calculateOccupancyRateForWeek() {
        Map<String, Double> occupancyRates = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        long totalRooms = roomRepository.countTotalRooms(); //Tổng số phòng

        //Dữ liệu trạng thái phòng cho từng ngày
        for(int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            String day = date.getDayOfWeek().toString().substring(0, 3); //Cắt ngắn chuỗi thành Mon, Tue, ...

            //Lấy tất cả BookingRoom đang hoạt động trong ngày
            List<BookingRoom> activeBookings = bookingRoomRepository.findActiveBookingsOnDate(date);

            long occupiedRooms = activeBookings
                    .stream()
                    .mapToLong(br -> br.getRooms().size())
                    .sum();

            // Toán tử ba ngôi: Condition ? right_value : wrong value
            double rate = (totalRooms > 0) ? (double) occupiedRooms / totalRooms * 100 : 0.0; //Công thức tính tỷ lệ phòng được chọn nhiều nhất

            occupancyRates.put(day, rate);
        }
        return occupancyRates;
    }

}
