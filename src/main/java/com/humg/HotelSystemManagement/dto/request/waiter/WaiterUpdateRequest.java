package com.humg.HotelSystemManagement.dto.request.waiter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaiterUpdateRequest {

    @Email(message = "INVALID_EMAIL")
    String email;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;
}
