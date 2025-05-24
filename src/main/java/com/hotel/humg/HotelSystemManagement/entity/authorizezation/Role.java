package com.hotel.humg.HotelSystemManagement.entity.authorizezation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true) //Cho phép thêm các trường được đánh dấu vào toString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @ToString.Include //Thêm trường name vào toString
    String name;
    String description;

    @ManyToMany
    Set<Permission> permissions;

    /**
     *     ACCOUNTANT,
     *     DEPARTMENT_HEAD,
     *     RECEPTIONIST,
     *     CLEANER,
     *     WAITER,
     *     ADMIN
     */


}
