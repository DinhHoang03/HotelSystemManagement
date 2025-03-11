package com.humg.HotelSystemManagement.entity.authorizezation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission{
    @Id
    String permissionName;
    String description;
}
