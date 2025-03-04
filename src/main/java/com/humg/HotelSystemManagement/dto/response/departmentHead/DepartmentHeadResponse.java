package com.humg.HotelSystemManagement.dto.response.departmentHead;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentHeadResponse {
    Long id;

    String name;

    String email;

    String phone;

    String role;
}
