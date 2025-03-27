package com.humg.HotelSystemManagement.dto.response.roomServiceStatus;

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
