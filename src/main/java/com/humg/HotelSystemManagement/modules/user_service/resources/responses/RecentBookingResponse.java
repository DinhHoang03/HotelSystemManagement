package com.humg.HotelSystemManagement.modules.user_service.resources.responses;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class RecentBookingResponse {
    private String customerName; // Cột Customer
    private String roomName;     // Cột Room
    private LocalDate checkInDate; // Cột Check-in
    private String status;       // Cột Status
    private Double amount;       // Cột Amount (kiểu Double hoặc Long tùy DB của bạn)
}