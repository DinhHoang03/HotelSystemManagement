package com.humg.HotelSystemManagement.modules.ai_service.controllers;

import com.humg.HotelSystemManagement.modules.ai_service.resources.requests.PromptRequest;
import com.humg.HotelSystemManagement.utils.APIResponse;
import com.humg.HotelSystemManagement.modules.ai_service.services.GeminiService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gemini")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiController {
    GeminiService genemiService;

    @PostMapping("/ask")
    APIResponse<String> askQuestion(@RequestBody PromptRequest request) {
        String answer = genemiService.getChatAnswer(request);
        return APIResponse.<String>builder()
                .result(answer)
                .message("Chat bot reply successfully!")
                .build();
    }
}
