package com.humg.HotelSystemManagement.entity;

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

    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id")
    Booking booking;

    @OneToOne(mappedBy = "bookingBill", cascade = CascadeType.ALL, orphanRemoval = true)
    Payment payment;
}
