package com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "hotel_offers")
public class HotelOffers {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hotel_offer_id")
    String hotelServiceId;

    @Column(name = "service_category", nullable = false)
    String serviceCategory;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "price", nullable = false)
    Long price;

    @Column(name = "unit_info")
    String unitInfo;

    // FIX TẠI ĐÂY: Thêm @Builder.Default
    @OneToMany(mappedBy = "hotelOffers", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    List<BookingItems> bookingItems = new ArrayList<>();
}