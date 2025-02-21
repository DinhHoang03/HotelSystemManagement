package com.humg.HotelSystemManagement.entity.employees;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Accountant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long accountantId;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true, length = 12)
    String phone;

    @Column(nullable = false)
    String password;

    @OneToMany(mappedBy = "accountant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Employee> employees = new ArrayList<>();
}
