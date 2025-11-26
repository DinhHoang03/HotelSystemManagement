package com.humg.HotelSystemManagement.modules.room_service.resources.responses;

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

    String imageUrl;
    String description;

    Long halfDayPrice;
    Long fullDayPrice;
    Long fullWeekPrice;

    Integer maxAdults;
    Integer maxChildren;
    Double area;
    String amenities;
}