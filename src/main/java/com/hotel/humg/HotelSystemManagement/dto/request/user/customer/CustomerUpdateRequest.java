package com.hotel.humg.HotelSystemManagement.dto.request.user.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerUpdateRequest {

    @Size(max = 50, message = "INVALID_NAME")
    String name;

    @Size(max = 40, message = "INVALID_USERNAME")
    String username;

    @Email(message = "INVALID_EMAIL")
    String email;

    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;
    
    @Size(max = 200, message = "INVALID_ADDRESS")
    String address;
}
