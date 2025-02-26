package com.humg.HotelSystemManagement.dto.response.cleaner;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CleanerResponse {
    Long cleanerId;

    String name;

    String email;

    String phone;
}
