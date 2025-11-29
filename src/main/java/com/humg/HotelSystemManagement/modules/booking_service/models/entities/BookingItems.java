package com.humg.HotelSystemManagement.modules.booking_service.models.entities;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status") // Đặt tên cột là item_status hoặc booking_status tùy bạn
    BookingStatus bookingStatus;

    @Column(nullable = true)
    String username; // Người gọi dịch vụ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_offer_id", nullable = false)
    HotelOffers hotelOffers;
}