package com.hotel.humg.HotelSystemManagement.dto.request.admin;

import com.hotel.humg.HotelSystemManagement.entity.enums.UserStatus;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FindEmpStatusRequest {
    @Pattern(regexp = "^(PENDING|APPROVED|REJECTED|OFFLINE|ONLINE)$",
    message = "INVALID_STATUS")
    UserStatus userStatus;
    int page;
    int size;
}

