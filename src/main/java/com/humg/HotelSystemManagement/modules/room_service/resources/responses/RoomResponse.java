package com.humg.HotelSystemManagement.modules.room_service.resources.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomResponse {
    Long roomId;
    String roomNumber;
    String roomStatus; // AVAILABLE, OCCUPIED

    // --- NEW FIELDS ---
    Integer floor;
    String viewType;
    boolean isClean;   // Trạng thái vệ sinh

    // Thông tin loại phòng (Flatten ra cho dễ dùng)
    Long roomTypeId;
    String roomTypeName;
    String imageUrl;
    Long priceByDay;
    Integer maxAdults; // Để biết phòng này chứa đc bao nhiêu người
}