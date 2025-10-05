package com.humg.HotelSystemManagement.modules.ai_service.resources.responses;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {
    List<Candidate> candidates;
}
