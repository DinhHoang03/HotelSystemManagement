package com.humg.HotelSystemManagement.modules.room_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    String roomNumber;
    Long roomTypeId;

    // --- NEW FIELDS ---
    Integer floor;
    String viewType; // Sea View, City View

    Boolean isClean; // Dùng khi tạp vụ update trạng thái (True/False)
}