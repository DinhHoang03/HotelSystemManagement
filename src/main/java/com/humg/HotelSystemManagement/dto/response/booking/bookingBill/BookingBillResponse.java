package com.humg.HotelSystemManagement.dto.response.booking.bookingBill;

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
    LocalDate issueDate;
}
