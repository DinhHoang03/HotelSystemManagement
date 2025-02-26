package com.humg.HotelSystemManagement.dto.request.cleaner;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CleanerUpdateRequest {
    @Email(message = "INVALID_EMAIL")
    String email;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;
}
