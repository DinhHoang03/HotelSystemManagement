package com.humg.HotelSystemManagement.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String bookingId;
    LocalDate bookingDate;
    String bookingStatus;
    Long totalRoomPrice;
    Long totalBookingServicePrice;
    Long grandTotal;
}
