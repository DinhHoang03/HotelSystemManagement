package com.humg.HotelSystemManagement.modules.room_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeRequest {
    String roomTypes;
    Long halfDayPrice;
    Long fullDayPrice;
    Long fullWeekPrice;
}
