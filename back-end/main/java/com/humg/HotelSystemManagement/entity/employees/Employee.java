package com.humg.HotelSystemManagement.entity.employees;

import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import com.humg.HotelSystemManagement.entity.staffManagerment.Contract;
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
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    Long empId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id", nullable = false)
    Waiter waiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionist_id", nullable = false)
    Receptionist receptionist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_id", nullable = false)
    Cleaner cleaner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountant_id", nullable = false)
    Accountant accountant;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Attendance> attendances = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    Contract contract;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String role;
}
