package com.humg.HotelSystemManagement.entity.booking;

import com.humg.HotelSystemManagement.entity.enums.PaymentMethod;
import com.humg.HotelSystemManagement.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    Long paymentId;

    @Column(name = "transaction_id", nullable = false, unique = true)
    String transactionId;

    @Column(name = "paid_amount", nullable = false)
    Long paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    PaymentMethod  paymentMethod;

    @Column(name = "create_at", nullable = false)
    LocalDate createAt;

    @Column(name = "update_at", nullable = false)
    LocalDate updateAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_bill_id", referencedColumnName =  "booking_bill_id", unique = true)
    BookingBill bookingBill;
}
