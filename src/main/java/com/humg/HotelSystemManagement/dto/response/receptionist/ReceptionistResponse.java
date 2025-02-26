package com.humg.HotelSystemManagement.dto.response.receptionist;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReceptionistResponse {
    Long receptionistId;

    String name;

    String email;

    String phone;
}
