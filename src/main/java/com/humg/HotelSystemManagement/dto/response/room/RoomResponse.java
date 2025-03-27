package com.humg.HotelSystemManagement.dto.response.room;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomType;
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
