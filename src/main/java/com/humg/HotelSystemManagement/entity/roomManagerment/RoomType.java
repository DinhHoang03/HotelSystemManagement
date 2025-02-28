package com.humg.HotelSystemManagement.entity.roomManagerment;

import com.humg.HotelSystemManagement.entity.enums.RoomTypes;
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

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    List<Room> rooms = new ArrayList<>();

    @PrePersist
    public void setPrices(){
        if(halfDayPrice == null){
            this.halfDayPrice = switch (roomTypes){
                case RoomTypes.STANDARD -> halfDayPrice = 500000L;
                case RoomTypes.SUPERIOR -> halfDayPrice = 700000L;
                case RoomTypes.DELUXE -> halfDayPrice = 1000000L;
            };
        }

        if(fullDayPrice == null){
            this.fullDayPrice = switch (roomTypes){
                case RoomTypes.STANDARD -> fullDayPrice = 800000L;
                case RoomTypes.SUPERIOR -> fullDayPrice = 1200000L;
                case RoomTypes.DELUXE -> fullDayPrice = 1800000L;
            };
        }

        if(fullWeekPrice == null){
            this.fullWeekPrice = switch (roomTypes){
                case RoomTypes.STANDARD -> fullWeekPrice = 5000000L;
                case RoomTypes.SUPERIOR -> fullWeekPrice = 7500000L;
                case RoomTypes.DELUXE -> fullWeekPrice = 12000000L;
            };
        }
    }
}
