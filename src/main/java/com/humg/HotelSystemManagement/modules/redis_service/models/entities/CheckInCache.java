package com.humg.HotelSystemManagement.modules.redis_service.models.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@RedisHash(value = "CheckIn", timeToLive = 86400) //Thời hạn: 1 ngày
public class CheckInCache implements Serializable {
    @Id
    String employeeId;
    LocalDateTime checkInTime;
}
