package com.humg.HotelSystemManagement.entity.employees;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomService;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Waiter extends Employee{
    @OneToMany(mappedBy = "waiter", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<RoomService> roomServices = new ArrayList<>();

    @OneToMany(mappedBy = "waiter", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<EmployeeList> employees = new ArrayList<>();
}
