package com.humg.HotelSystemManagement.dto.request.waiter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaiterCreationRequest {
    @NotNull(message = "REQUEST_NULL")
    String name;

    @Email(message = "INVALID_EMAIL")
    @NotNull(message = "REQUEST_NULL")
    String email;

    @NotNull(message = "REQUEST_NULL")
    @Size(max = 12, message = "INVALID_PHONE_NUMBER")
    String phone;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$", message = "INVALID_PASSWORD")
    @NotNull(message = "REQUEST_NULL")
    String password;
}
