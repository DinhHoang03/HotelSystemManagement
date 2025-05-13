package com.humg.HotelSystemManagement.service.UserService;

import com.humg.HotelSystemManagement.dto.response.user.employee.EmployeeResponse;
import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.entity.User.Employee;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.EmployeeMapper;
import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
import com.humg.HotelSystemManagement.repository.booking.PaymentBillRepository;
import com.humg.HotelSystemManagement.repository.User.CustomerRepository;
import com.humg.HotelSystemManagement.repository.User.EmployeeRepository;
import com.humg.HotelSystemManagement.repository.roomManagerment.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {

    EmployeeRepository employeeRepository;
    CustomerRepository customerRepository;
    RoomRepository roomRepository;
    BookingRoomRepository bookingRoomRepository;
    PaymentBillRepository paymentBillRepository;
    BookingRepository bookingRepository;
    EmployeeMapper employeeMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse approveEmployee(String id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(employee.getUserStatus() == UserStatus.PENDING || employee.getUserStatus() == UserStatus.REJECTED){
            employee.setUserStatus(UserStatus.APPROVED);
            employeeRepository.save(employee);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return employeeMapper.toEmployeeResponse(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse rejectEmployee(String id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if(employee.getUserStatus() == UserStatus.PENDING || employee.getUserStatus() == UserStatus.APPROVED){
            employee.setUserStatus(UserStatus.REJECTED);
            employeeRepository.save(employee);
        }else{
            throw new AppException(AppErrorCode.INVALID_STATUS);
        }

        return employeeMapper.toEmployeeResponse(employee);
    }

    public Page<EmployeeResponse> findAllByStatusEmployee(int page, int size, UserStatus userStatus) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Employee> employees = employeeRepository.findByUserStatus(userStatus, pageable);
        if(employees.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        Page<EmployeeResponse> response = employees.map(employee -> {
            return EmployeeResponse.builder()
                    .id(employee.getId())
                    .username(employee.getUsername())
                    .name(employee.getName())
                    .gender(employee.getGender().toString())
                    .dob(employee.getDob())
                    .email(employee.getEmail())
                    .phone(employee.getPhone())
                    .address(employee.getAddress())
                    .identityId(employee.getIdentityId())
                    .userStatus(employee.getUserStatus().toString())
                    .build();
        });
        return response;
    }

    public Long countEmployeeByList() {
        var count = employeeRepository.count();
        return count;
    }

    public Long countCustomerByList() {
        var count = customerRepository.count();
        return count;
    }

    public Long countRoomByList() {
        var count = roomRepository.count();
        return count;
    }

    public Long countBookingTodayByList(LocalDate now) {
        List<Booking> bookingToday = bookingRepository.getBookingsToday(now);

        long result = bookingToday.size();

        return result;
    }

    public Long getTodayRevenue(LocalDate now) {
        var revenue = paymentBillRepository.getTodayRenevue(now);
        return revenue;
    }

    public Long totalCountUser() {
        var empC = employeeRepository.count();
        var cusC = customerRepository.count();
        var totalC = empC + cusC;

        return totalC;
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
