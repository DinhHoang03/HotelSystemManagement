package com.humg.HotelSystemManagement.modules.payment_service.models.entities;

import com.humg.HotelSystemManagement.modules.customer_service.models.entities.User;
import com.humg.HotelSystemManagement.utils.enums.PaymentMethod;
import com.humg.HotelSystemManagement.utils.enums.PaymentStatus;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment")
public class PaymentBill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    String paymentId;

    @Column(name = "transaction_id", nullable = false, unique = true)
    String transactionId;

    @Column(name = "paid_amount", nullable = false)
    Long paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    PaymentMethod paymentMethod;

    @Column(name = "create_at", nullable = false)
    LocalDate createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}