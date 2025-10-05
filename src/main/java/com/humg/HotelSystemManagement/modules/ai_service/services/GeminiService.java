package com.humg.HotelSystemManagement.modules.ai_service.services;

import com.humg.HotelSystemManagement.modules.ai_service.resources.requests.PromptRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiService {
    ChatClient chatClient;

    public GeminiService(ChatClient.Builder builder) {
        chatClient = builder.build();
    }

    public String getChatAnswer(PromptRequest request) {
        SystemMessage systemMessage = new SystemMessage(
                """
                You are a hotel helper for customer from DinhRiseHotel
                """
        );

        UserMessage userMessage = new UserMessage(request.getMessage());
        Prompt prompt = new Prompt(systemMessage, userMessage);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
