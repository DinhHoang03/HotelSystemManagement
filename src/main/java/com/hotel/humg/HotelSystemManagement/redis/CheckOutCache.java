package com.hotel.humg.HotelSystemManagement.redis;

import org.springframework.data.annotation.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@RedisHash(value = "CheckOut", timeToLive = 86400) //Thời hạn: 1 ngày
public class CheckOutCache implements Serializable {
    @Id
    String employeeId;
    LocalDateTime checkOutTime;
}
