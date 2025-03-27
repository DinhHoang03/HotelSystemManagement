package com.humg.HotelSystemManagement.configuration.payment.zaloPay;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayContext {
    int appId;
    String key1;
    String key2;
    String createOrderUrl;
}
