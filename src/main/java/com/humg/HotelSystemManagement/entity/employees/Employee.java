package com.humg.HotelSystemManagement.entity.employees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true, length = 12)
    String phone;

    @Column(nullable = false)
    String password;
}
