package com.humg.HotelSystemManagement.entity.employees;

import com.humg.HotelSystemManagement.entity.staffManagerment.Attendance;
import com.humg.HotelSystemManagement.entity.staffManagerment.Contract;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
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
public class EmployeeList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    Long empId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id")
    Waiter waiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionist_id")
    Receptionist receptionist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_id")
    Cleaner cleaner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountant_id")
    Accountant accountant;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Attendance> attendances = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    Contract contract;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String role;


    //Hàm set role tương ứng của các employee
    @PrePersist
    @PreUpdate
    public void setRoleAndName(){
        if(waiter != null){
            this.role = waiter.getClass().getSimpleName().toUpperCase();
            this.name = waiter.getName();
        }else if(accountant != null){
            this.role = accountant.getClass().getSimpleName().toUpperCase();
            this.name = accountant.getName();
        }else if(admin != null){
            this.role = admin.getClass().getSimpleName().toUpperCase();
            this.name = admin.getName();
        }else if(cleaner != null){
            this.role = cleaner.getClass().getSimpleName().toUpperCase();
            this.name = cleaner.getName();
        }else if(receptionist != null){
            this.role = receptionist.getClass().getSimpleName().toUpperCase();
            this.name = receptionist.getName();
        }else{
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }
    }
}
