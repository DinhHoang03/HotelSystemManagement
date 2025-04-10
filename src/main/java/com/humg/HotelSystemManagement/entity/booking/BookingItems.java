package com.humg.HotelSystemManagement.entity.booking;

import com.humg.HotelSystemManagement.entity.totalServices.HotelOffers;
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
    String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_offer_id", nullable = false)
    HotelOffers hotelOffers;
}
