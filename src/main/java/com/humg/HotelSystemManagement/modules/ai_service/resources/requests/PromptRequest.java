package com.humg.HotelSystemManagement.modules.ai_service.resources.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromptRequest {
    String message;
}
