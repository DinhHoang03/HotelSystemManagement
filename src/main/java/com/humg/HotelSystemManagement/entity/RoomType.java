package com.humg.HotelSystemManagement.entity;

import com.humg.HotelSystemManagement.entity.enums.RoomTypes;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    Long roomTypeId;

    @Column(name = "room_types", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    RoomTypes roomTypes;

    @Column(name = "half_day_price", nullable = false)
    Long halfDayPrice;

    @Column(name = "full_day_price", nullable = false)
    Long fullDayPrice;

    @Column(name = "full_week_price", nullable = false)
    Long fullWeekPrice;

    @PrePersist
    public void setHalfDayPrice(){
        if(halfDayPrice == null){
            this.halfDayPrice = switch (roomTypes){
                case Room
            }
        }
    }
}
