package com.humg.HotelSystemManagement.modules.room_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeRequest {
    String roomTypes; // Tên loại phòng: VIP, Standard...

    // --- NEW FIELDS ---
    String imageUrl;
    String description;

    // Giá
    Long halfDayPrice;
    Long fullDayPrice;
    Long fullWeekPrice;

    // Sức chứa & Tiện ích
    Integer maxAdults;
    Integer maxChildren;
    Double area;
    String amenities; // "Wifi, Tivi, AC"
}