package com.humg.HotelSystemManagement.entity.User;

import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import com.humg.HotelSystemManagement.entity.enums.Gender;
import com.humg.HotelSystemManagement.entity.enums.UserStatus;
import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import com.humg.HotelSystemManagement.entity.staffManagerment.Contract;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;

    String password;

    String name;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate dob;

    @Column(unique = true)
    String email;

    @Column(unique = true, length = 12)
    String phone;

    @Enumerated(EnumType.STRING)
    UserStatus userStatus;

    @Column(unique = true, length = 12)
    String identityId;

    String address;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Attendance> attendances = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    Contract contract;
}
