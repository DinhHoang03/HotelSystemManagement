package com.hotel.humg.HotelSystemManagement.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayPalOrderRequest {
    String bookingBillId;
}
