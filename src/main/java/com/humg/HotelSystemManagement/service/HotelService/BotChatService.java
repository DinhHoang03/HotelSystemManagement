package com.humg.HotelSystemManagement.service.HotelService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humg.HotelSystemManagement.dto.request.chatbot.ChatBotRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BotChatService {
    /**
     * Giải thích: Lớp này gửi payload JSON với type: "event_received" và text đến endpoint của Botpress.
     * Thay botId bằng ID bot thực tế của bạn (ví dụ: my-bot)
     */
    RestTemplate template;

    @Value("${botpress.url}")
    @NonFinal
    String botpressUrl;

    @Value("${botpress.bot-id}")
    @NonFinal
    String botId;

    public String sendMessageToBotpress(ChatBotRequest chatBotRequest) {
        String message = chatBotRequest.getMessage();
        String url = botpressUrl + botId + "/converse";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = String.format("{\"type\": \"event_received\", \"text\": \"%s\"}", message);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            String response = template.postForObject(url, request, String.class);

            //Parse JSON để lấy tin nhắn
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseArray = objectMapper.readTree(response);
            if(responseArray.isArray() && responseArray.size() > 0) {
                JsonNode firstMessage = responseArray.get(0);
                if(firstMessage.has("text")) {
                    return firstMessage.get("text").asText();
                }
            }
            return "No response from Botpress";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending message to Botpress: " + e.getMessage();
        }
    }
}
