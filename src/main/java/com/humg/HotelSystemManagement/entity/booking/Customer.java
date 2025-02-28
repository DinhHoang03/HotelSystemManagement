package com.humg.HotelSystemManagement.entity.booking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    Long id;

    @Column(name = "identity_id", nullable = false, unique = true)
    String identityId;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String phone;

    @Column(nullable = false)
    String email;

    @JsonIgnore
    @Column(nullable = false)
    String password;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Booking> bookings = new ArrayList<>();
}
