package com.humg.HotelSystemManagement.entity.employees;

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
public class Accountant extends Employee{
    @OneToMany(mappedBy = "accountant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<EmployeeList> employees = new ArrayList<>();
}
