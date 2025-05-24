package com.hotel.humg.HotelSystemManagement.dto.response.room.roomType;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeResponse {
    Long roomTypeId;
    String roomTypes;
    Long halfDayPrice;
    Long fullDayPrice;
    Long fullWeekPrice;
}
