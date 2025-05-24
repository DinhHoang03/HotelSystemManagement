package com.hotel.humg.HotelSystemManagement.controller.AIController;

import com.hotel.humg.HotelSystemManagement.dto.response.APIResponse;
import com.hotel.humg.HotelSystemManagement.service.AIService.GeminiService;
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
    APIResponse<String> askQuestion(@RequestBody String question) {
        String answer = genemiService.getAnswer(question);
        return APIResponse.<String>builder()
                .result(answer)
                .message("Chat bot reply successfully!")
                .build();
    }
}
