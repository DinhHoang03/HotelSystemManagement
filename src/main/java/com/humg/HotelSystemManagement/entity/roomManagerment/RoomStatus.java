package com.humg.HotelSystemManagement.entity.roomManagerment;

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
public class RoomStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_status_id")
    Long roomStatusId;

    @Column(name = "room_status", nullable = false)
    //@Enumerated(EnumType.STRING)
    String roomStatus;

    @Column(columnDefinition = "TEXT", nullable = false)
    String description;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_id", nullable = false)
//    Room room;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "waiter_id", nullable = false)
//    Employee employee;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", unique = true)
    Room room;
}
