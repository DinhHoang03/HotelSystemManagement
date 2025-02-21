package com.humg.HotelSystemManagement.entity.booking;

import com.humg.HotelSystemManagement.entity.totalServices.HotelService;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_service_id")
    Long bookingServiceId;

    int quantity;

    @Column(name = "total_booking_service_price")
    Long totalBookingServicePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_service_id", nullable = false)
    HotelService hotelService;
}
