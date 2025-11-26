package com.humg.HotelSystemManagement.modules.room_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    Long roomTypeId;

    @Column(name = "room_types", nullable = false, unique = true)
    String roomTypes; // Standard, Deluxe, VIP...

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    // --- GIÁ TIỀN (Giữ nguyên) ---
    @Column(name = "half_day_price")
    Long halfDayPrice;

    @Column(name = "full_day_price")
    Long fullDayPrice;

    @Column(name = "full_week_price")
    Long fullWeekPrice;

    // --- MỞ RỘNG 1: SỨC CHỨA (Quan trọng để check logic đặt phòng) ---
    @Column(name = "max_adults", nullable = false)
    @Builder.Default
    Integer maxAdults = 2; // Tối đa người lớn

    @Column(name = "max_children", nullable = false)
    @Builder.Default
    Integer maxChildren = 1; // Tối đa trẻ em

    @Column(name = "area_m2")
    Double area; // Diện tích phòng (ví dụ: 30.5 m2)

    // --- MỞ RỘNG 2: TIỆN ÍCH (Amenities) ---
    // Lưu dạng chuỗi JSON hoặc CSV đơn giản: "Wifi, Tivi, Bồn tắm, Ban công"
    // Để hiển thị icon lên web cho đẹp
    @Column(name = "amenities", columnDefinition = "TEXT")
    String amenities;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Room> rooms = new ArrayList<>();
}