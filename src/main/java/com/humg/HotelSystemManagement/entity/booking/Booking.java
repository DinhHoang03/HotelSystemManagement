package com.humg.HotelSystemManagement.entity.booking;

import com.humg.HotelSystemManagement.entity.enums.BookingStatus;
import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    Long bookingId;

    @Column(name = "booking_date", nullable = false)
    LocalDate bookingDate;

    @Column(name = "booking_status", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    BookingStatus bookingStatus;

    @Column(name = "total_room_price", nullable = false)
    Long totalRoomPrice;

    @Column(name = "total_booking_service_price")
    Long totalBookingServicePrice;

    @Column(name = "grand_total", nullable = false)
    Long grandTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    BookingBill bookingBill;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingService> bookingServices;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    List<BookingRoom> bookingRooms;
}
