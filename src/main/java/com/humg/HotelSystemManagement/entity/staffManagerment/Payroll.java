package com.humg.HotelSystemManagement.entity.staffManagerment;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long payrollId;

    @Column(name = "pay_date")
    LocalDate payDate;

    @Column(name = "total_work_hours")
    Long totalWorkHours;

    Long salary;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", nullable = false)
    Contract contract;
}
