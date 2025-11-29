package com.humg.HotelSystemManagement.modules.payment_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayOrderRequest {
    String bookingId;
} 