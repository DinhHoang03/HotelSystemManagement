package com.humg.HotelSystemManagement.modules.booking_service.models.entities;

import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "booking_room")
public class BookingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_room_id")
    String bookingRoomId;

    @Column(name = "check_in_date", nullable = false)
    LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    LocalDate checkOutDate;

    @Column(name = "total_room_amount", nullable = false)
    Long totalRoomAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingStatus bookingStatus;

    @Column(nullable = true)
    String username; // Người đặt phòng này (có thể khác người book chính)

    // Logic cũ của bạn: 1 BookingRoom chứa nhiều Room thực tế
    // Lưu ý: Cascade MERGE/PERSIST để khi lưu BookingRoom thì cập nhật trạng thái Room
    @OneToMany(mappedBy = "bookingRoom", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    List<Room> rooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;
}