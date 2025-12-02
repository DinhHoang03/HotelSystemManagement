package com.humg.HotelSystemManagement.modules.booking_service.models.entities;

import com.humg.HotelSystemManagement.modules.user_service.models.entities.User;
import com.humg.HotelSystemManagement.modules.payment_service.models.entities.PaymentBill;
import com.humg.HotelSystemManagement.utils.enums.BookingStatus;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    String bookingId;

    @Column(name = "booking_date", nullable = false)
    LocalDate bookingDate;

    // --- TRẠNG THÁI ---
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    PaymentStatus paymentStatus;

    // --- THANH TOÁN ---
    @Column(name = "payment_date")
    LocalDateTime paymentDate;

    @Column(name = "payment_transaction_id")
    String paymentTransactionId;

    // --- TÀI CHÍNH ---
    @Column(name = "total_room_price")
    @Builder.Default
    Long totalRoomPrice = 0L;

    @Column(name = "total_service_price")
    @Builder.Default
    Long totalServicePrice = 0L;

    @Column(name = "actual_check_out_date")
    LocalDateTime actualCheckOutDate;

    @Column(name = "surcharge")
    @Builder.Default
    Long surcharge = 0L;

    @Column(name = "grand_total")
    @Builder.Default
    Long grandTotal = 0L;

    // --- QUAN HỆ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PaymentBill> paymentBills = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingRoom> bookingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<BookingItems> bookingItems = new ArrayList<>();

}