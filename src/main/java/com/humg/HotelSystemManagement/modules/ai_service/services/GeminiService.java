package com.humg.HotelSystemManagement.modules.ai_service.services;

import com.humg.HotelSystemManagement.modules.ai_service.resources.requests.PromptRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List; // Nhớ import List

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiService {
    ChatClient chatClient;

    // Dùng ChatClient.Builder là chuẩn cho bản Spring AI mới
    public GeminiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String getChatAnswer(PromptRequest request) {
        SystemMessage systemMessage = new SystemMessage(
                """
                You are a hotel helper for user from DinhRiseHotel. Answer nicely and concisely.
                """
        );

        UserMessage userMessage = new UserMessage(request.getMessage());

        // --- ĐOẠN SỬA QUAN TRỌNG ---
        // Lỗi cũ: new Prompt(systemMessage, userMessage);
        // Sửa lại: Phải gói vào List.of(...)
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}