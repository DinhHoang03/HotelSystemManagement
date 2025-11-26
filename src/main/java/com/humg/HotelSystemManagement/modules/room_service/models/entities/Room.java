package com.humg.HotelSystemManagement.modules.room_service.models.entities;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingRoom;
import com.humg.HotelSystemManagement.utils.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_number", nullable = false, unique = true)
    String roomNumber; // P101, P202

    // --- MỞ RỘNG 1: VỊ TRÍ ---
    @Column(name = "floor")
    Integer floor; // Tầng mấy (1, 2, 3...) -> Để khách chọn tầng cao/thấp

    // --- MỞ RỘNG 2: ĐẶC ĐIỂM RIÊNG ---
    @Column(name = "view_type")
    String viewType; // "Sea View" (Hướng biển), "City View", "Garden View"
    // Cái này quan trọng vì cùng là loại VIP nhưng view biển giá có thể khác hoặc khách thích hơn.

    // --- MỞ RỘNG 3: TRẠNG THÁI VỆ SINH ---
    // RoomStatus (AVAILABLE/BOOKED) chỉ nói về việc đặt phòng.
    // Cần thêm biến này để bộ phận buồng phòng (Housekeeping) biết.
    @Column(name = "is_clean")
    @Builder.Default
    boolean isClean = true; // true: Sạch, false: Chưa dọn (Sau khi khách checkout)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_room_id", nullable = true)
    BookingRoom bookingRoom;

    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus;
}