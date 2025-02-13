package com.humg.HotelSystemManagement.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    Long customerId;

    @Column(name = "identity_id", nullable = false, unique = true, length = 12)
    String identityId;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, length = 11)
    String phone;
    @Column(nullable = false)
    String email;
    @Column(nullable = false)
    String password;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Booking> bookings;
}
