package com.humg.HotelSystemManagement.modules.booking_service.resources.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingBillResponse {
    String bookingBillId;
    String bookingId;
    Long remainingAmount;
    Long grandTotal;
    LocalDate issueDate;
    LocalDate paymentDate;
}
