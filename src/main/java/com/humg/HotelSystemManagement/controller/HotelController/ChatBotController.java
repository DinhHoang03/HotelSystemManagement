package com.humg.HotelSystemManagement.controller.HotelController;

import com.humg.HotelSystemManagement.dto.request.chatbot.ChatBotRequest;
import com.humg.HotelSystemManagement.dto.response.APIResponse;
import com.humg.HotelSystemManagement.service.HotelService.BotChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatBotController {
    BotChatService botChatService;

    @PostMapping("/send")
    APIResponse<String> sendMessage(@RequestBody ChatBotRequest chatBotRequest) {
        return APIResponse.<String>builder()
                .result(botChatService.sendMessageToBotpress(chatBotRequest))
                .message("Successfully send message to bot!")
                .build();
    }
}
