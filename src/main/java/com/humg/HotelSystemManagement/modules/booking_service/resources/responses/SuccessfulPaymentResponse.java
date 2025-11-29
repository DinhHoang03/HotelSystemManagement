package com.humg.HotelSystemManagement.modules.booking_service.resources.responses;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.Booking;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuccessfulPaymentResponse {
    Booking booking;
    PaymentStatus status;
}
