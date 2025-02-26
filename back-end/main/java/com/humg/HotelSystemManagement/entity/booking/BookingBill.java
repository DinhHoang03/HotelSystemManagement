package com.humg.HotelSystemManagement.entity.booking;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_bill_id")
    Long bookingBillId;

    @Column(name = "issue_date")
    LocalDate issueDate;

    @Column(name = "grand_total", nullable = false)
    Long grandTotal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id", unique = true)
    Booking booking;

    @OneToOne(mappedBy = "bookingBill", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    Payment payment;
}
