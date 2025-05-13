package com.humg.HotelSystemManagement.service.EmployeeService;

import com.humg.HotelSystemManagement.dto.response.user.employee.AttendanceResponse;
import com.humg.HotelSystemManagement.dto.response.user.employee.CheckInResponse;
import com.humg.HotelSystemManagement.dto.response.user.employee.CheckOutResponse;
import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.redis.CheckInCache;
import com.humg.HotelSystemManagement.redis.CheckOutCache;
import com.humg.HotelSystemManagement.repository.User.EmployeeRepository;
import com.humg.HotelSystemManagement.repository.redis.CheckInRepository;
import com.humg.HotelSystemManagement.repository.redis.CheckOutRepository;
import com.humg.HotelSystemManagement.repository.staffManagerment.AttendanceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceService {
    AttendanceRepository attendanceRepository;
    EmployeeRepository employeeRepository;
    CheckInRepository checkInRepository;
    CheckOutRepository checkOutRepository;

    public AttendanceResponse createAttendance(String username) {
        var employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        var name = employee.getName();
        var employeeId = employee.getId();

        //Làm logic kiểm tra username
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); //Không cần format

        //Lỗi ở đây
        var checkInCache = checkInRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(AppErrorCode.UNVALID_CHECK_DATE));
        var checkOutCache = checkOutRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(AppErrorCode.UNVALID_CHECK_DATE));

        var checkIn = checkInCache.getCheckInTime();
        var checkOut = checkOutCache.getCheckOutTime();

        Long caculatedWorkHour = Duration.between(checkIn, checkOut).toHours();

        Attendance attendance = Attendance.builder()
                .checkIn(checkIn)
                .checkOut(checkOut)
                .workHour(caculatedWorkHour)
                .employee(employee)
                .build();

        var result = attendanceRepository.save(attendance);

        var response = AttendanceResponse.builder()
                .attendanceId(result.getAttendanceId())
                .checkIn(result.getCheckIn().format(format))
                .checkOut(result.getCheckOut().format(format))
                .workHour(result.getWorkHour())
                .employeeName(result.getEmployee().getName())
                .build();

        return response;
    }

    //Làm thêm get my attendance và get all by paging(admin permission)
    public Page<AttendanceResponse> getMyAttendances(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        var employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));

        Page<Attendance> attendance = attendanceRepository.findByEmployee(employee, pageable);
        if(attendance.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<AttendanceResponse> response = attendance.map(result -> {
            return AttendanceResponse.builder()
                    .attendanceId(result.getAttendanceId())
                    .checkIn(result.getCheckIn().toString())
                    .checkOut(result.getCheckOut().toString())
                    .workHour(result.getWorkHour())
                    .employeeName(result.getEmployee().getName())
                    .build();
        });

        return response;
    }

    //Admin Permission
    public Page<AttendanceResponse> getAllAttendances(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Attendance> attendance = attendanceRepository.findAll(pageable);
        if(attendance.isEmpty()) throw new AppException(AppErrorCode.LIST_EMPTY);

        Page<AttendanceResponse> response = attendance.map(result -> {
            return AttendanceResponse.builder()
                    .attendanceId(result.getAttendanceId())
                    .checkIn(result.getCheckIn().toString())
                    .checkOut(result.getCheckOut().toString())
                    .workHour(result.getWorkHour())
                    .employeeName(result.getEmployee().getName())
                    .build();
        });

        return response;
    }

    public CheckInResponse checkIn(String username) {
        if(username == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        var employeeName = employee.getName();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var checkIn = LocalDateTime.now();

        CheckInCache checkInCache = CheckInCache.builder()
                .employeeId(employee.getId())
                .checkInTime(checkIn)
                .build();

        var result = checkInRepository.save(checkInCache);

        var response = CheckInResponse.builder()
                .checkInDate(result.getCheckInTime().format(formatter))
                .employeeName(employeeName)
                .build();

        return response;
    }

    public CheckOutResponse checkOut(String username) {
        if(username == null) throw new AppException(AppErrorCode.REQUEST_IS_NULL);

        var employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.OBJECT_IS_NULL));
        var employeeName = employee.getName();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var checkOut = LocalDateTime.now();

        CheckOutCache checkOutCache = CheckOutCache.builder()
                .employeeId(employee.getId())
                .checkOutTime(checkOut)
                .build();

        var result = checkOutRepository.save(checkOutCache);

        var response = CheckOutResponse.builder()
                .checkOutDate(result.getCheckOutTime().format(formatter))
                .employeeName(employeeName)
                .build();

        return response;
    }
}
