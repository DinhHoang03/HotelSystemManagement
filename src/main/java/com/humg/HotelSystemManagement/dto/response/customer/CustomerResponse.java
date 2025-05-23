package com.humg.HotelSystemManagement.dto.response.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    Long id;

    String username;

    String name;

    String gender;

    LocalDate dob;

    String email;

    String phone;

    String identityId;

    String role;

    String address;
}
