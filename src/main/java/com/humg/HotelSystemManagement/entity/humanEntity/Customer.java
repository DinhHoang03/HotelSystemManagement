package com.humg.HotelSystemManagement.entity.humanEntity;

import com.humg.HotelSystemManagement.entity.booking.Booking;
import com.humg.HotelSystemManagement.entity.enums.Gender;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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

    @Column(unique = true)
    String username;

    String password;

    String name;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate dob;

    @Column(unique = true)
    String email;

    @Column(unique = true)
    String phone;

    @Column(unique = true)
    String identityId;

    String role;

    String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Booking> bookings = new ArrayList<>();
}
