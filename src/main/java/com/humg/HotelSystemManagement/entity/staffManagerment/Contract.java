package com.humg.HotelSystemManagement.entity.staffManagerment;

import com.humg.HotelSystemManagement.entity.employees.Employee;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level =AccessLevel.PRIVATE)
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    Long contactId;

    @Column(name = "contract_start")
    LocalDate contractStart;

    @Column(name = "contract_end")
    LocalDate contractEnd;

    @Column(name = "hourly_rate")
    Long hourlyRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", unique = true)
    Employee employee;

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    Payroll payroll;
}

