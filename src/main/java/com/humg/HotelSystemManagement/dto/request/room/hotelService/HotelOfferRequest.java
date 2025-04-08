package com.humg.HotelSystemManagement.dto.request.room.hotelService;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HotelOfferRequest {
    String serviceType;
    Long price;
}
