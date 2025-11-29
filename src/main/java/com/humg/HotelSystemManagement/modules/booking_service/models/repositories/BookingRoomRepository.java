package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, String> {

    // 1. API Mới: Lấy danh sách phòng ĐANG ĐI CHỢ (Cart)
    Page<BookingRoom> findByUsernameAndBookingStatus(String username, BookingStatus status, Pageable pageable);

    // 2. API Cũ: Lấy tất cả của user
    Page<BookingRoom> findByUsername(String username, Pageable pageable);

    // 3. Hỗ trợ Security check cho Booking Service (Tránh IDOR)
    List<BookingRoom> findByUsernameAndBookingRoomIdIn(String username, List<String> ids);

    // 4. Hỗ trợ check trùng lịch (Prevent Double Booking)
    @Query("SELECT br.bookingRoomId FROM BookingRoom br JOIN br.rooms r " +
            "WHERE r.roomNumber IN :roomNumbers " +
            "AND br.bookingStatus <> 'CANCELLED' " +
            "AND br.bookingStatus <> 'IN_CART' " + // Không check trùng với hàng trong giỏ, chỉ check đơn đã đặt
            "AND ((br.checkInDate < :checkOutDate) AND (br.checkOutDate > :checkInDate))")
    List<String> findBookedRoomNumbersInDateRangeForRooms(
            @Param("roomNumbers") List<String> roomNumbers,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    // 5. --- [FIX LỖI CỦA BẠN] ---: Hỗ trợ Dashboard thống kê (Occupancy Rate)
    // Tìm các booking đang active trong ngày cụ thể (để tính xem ngày đó có bao nhiêu phòng có khách)
    // Logic: Ngày đó nằm trong khoảng CheckIn và CheckOut, và trạng thái phải là CONFIRMED hoặc CHECKED_IN
    @Query("SELECT br FROM BookingRoom br " +
            "WHERE :date >= br.checkInDate " +
            "AND :date < br.checkOutDate " +
            "AND (br.bookingStatus = 'CONFIRMED' OR br.bookingStatus = 'CHECKED_IN' OR br.bookingStatus = 'PENDING')")
    List<BookingRoom> findActiveBookingsOnDate(@Param("date") LocalDate date);
}