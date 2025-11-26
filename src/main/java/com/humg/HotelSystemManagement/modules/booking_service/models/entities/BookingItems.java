package com.humg.HotelSystemManagement.modules.booking_service.models.entities;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
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
@Table(name = "booking_items")
public class BookingItems {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_item_id")
    String bookingItemId;

    int quantity;

    @Column(name = "total_booking_service_price")
    Long totalItemsPrice;

    @Column(nullable = true)
    String username; // Người gọi dịch vụ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_offer_id", nullable = false)
    HotelOffers hotelOffers;
}