package com.humg.HotelSystemManagement.dto.request.room;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    String roomNumber;
    String roomStatus;
    String roomType;
}
