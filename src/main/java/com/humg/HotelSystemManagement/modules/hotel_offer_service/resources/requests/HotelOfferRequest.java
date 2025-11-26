package com.humg.HotelSystemManagement.modules.hotel_offer_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HotelOfferRequest {
    // Loại dịch vụ: SPA, FOOD, DRINK, TRANSPORT...
    String serviceCategory;

    // Tên hiển thị: "Phở Bò", "Massage Thái"
    String name;

    // Mô tả chi tiết
    String description;

    // Giá tiền (Dùng Long hoặc BigDecimal)
    Long price;

    // Đơn vị: "Bát", "Suất", "60 Phút"
    String unitInfo;
}
