package com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests;

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
