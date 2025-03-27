package com.humg.HotelSystemManagement.dto.request.roomServiceStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomStatusRequest {
    String roomStatus;
    String description;
    String roomNumber;
}
