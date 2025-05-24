package com.hotel.humg.HotelSystemManagement.dto.response.ai.gemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {
    List<Candidate> candidates;
}
