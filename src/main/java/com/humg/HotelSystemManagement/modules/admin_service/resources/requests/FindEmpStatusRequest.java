package com.humg.HotelSystemManagement.modules.admin_service.resources.requests;

import com.humg.HotelSystemManagement.utils.enums.UserStatus;
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

