package com.humg.HotelSystemManagement.modules.room_service.resources.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomStatusResponse {
    String roomStatus;
    String description;
    String roomNumber;
}
