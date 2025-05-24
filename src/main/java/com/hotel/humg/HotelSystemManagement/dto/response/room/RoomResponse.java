package com.hotel.humg.HotelSystemManagement.dto.response.room;

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
    String roomStatus;
    String roomType;
}
