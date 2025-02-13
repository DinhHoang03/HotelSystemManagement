package com.humg.HotelSystemManagement.entity;

import com.humg.HotelSystemManagement.entity.enums.RoomStatus;
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
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_number", nullable = false, unique = true)
    String roomNumber;

    @Column(name = "room_status", nullable = false)
    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    RoomType roomType;
}
