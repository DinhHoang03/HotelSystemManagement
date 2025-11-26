package com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HotelOfferResponse {
    String id;
    String category;
    String name;
    String description;
    String imageUrl;
    Long price;
    String unitInfo;
}
