package com.humg.HotelSystemManagement.entity.roomManagerment;

import com.humg.HotelSystemManagement.entity.enums.RoomServiceStatus;
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
public class RoomService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_service_id")
    Long roomServiceID;

    @Column(name = "room_status")
    @Enumerated(EnumType.STRING)
    RoomServiceStatus roomServiceStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id", nullable = false)
    Waiter waiter;
}
