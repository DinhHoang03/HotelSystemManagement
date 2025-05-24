package com.hotel.humg.HotelSystemManagement.dto.request.chatbot;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatBotRequest {
    String message;
}
