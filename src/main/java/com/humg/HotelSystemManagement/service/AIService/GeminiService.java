package com.humg.HotelSystemManagement.service.AIService;

import com.humg.HotelSystemManagement.dto.response.ai.gemini.GeminiResponse;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiService {
    //We need: Access key API and URl to model we use
    @NonFinal
    @Value("${gemini.api-key}")
    String geminiAPIKey;

    @NonFinal
    @Value("${gemini.url}")
    String geminiAPIUrl;

    WebClient webClient;

    public String getAnswer(String question) {
        if(question.isEmpty()) throw new AppException(AppErrorCode.QUESTION_NOT_VALID);

        //Construct the request payload
        Map<String, Object> requestBody =
                Map.of("contents", new Object[] {
                        Map.of("parts", new Object[]{
                            Map.of("text", question)
                        })
                });

        //Make API call
        GeminiResponse response = webClient.post()
                .uri(geminiAPIUrl + "?key=" + geminiAPIKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();

        //Return ressponse
        return response
                .getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();
    }
}
