package com.humg.HotelSystemManagement.entity.booking;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "booking_bills")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingBill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_bill_id")
    String bookingBillId;

    @Column(name = "issue_date")
    LocalDate issueDate;

    @Column(name = "grand_total", nullable = false)
    Long grandTotal;

    @Column(name = "payment_date")
    LocalDate paymentDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id")
    Booking booking;

    @OneToMany(mappedBy = "bookingBill", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Payment> payments = new ArrayList<>();
}