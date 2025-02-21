package com.humg.HotelSystemManagement.entity.employees;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomService;
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
public class Waiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiter_id")
    Long waiterId;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true, length = 11)
    String phone;

    @Column(nullable = false)
    String password;

    @OneToMany(mappedBy = "waiter", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<RoomService> roomServices = new ArrayList<>();

    @OneToMany(mappedBy = "waiter", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Employee> employees = new ArrayList<>();
}
