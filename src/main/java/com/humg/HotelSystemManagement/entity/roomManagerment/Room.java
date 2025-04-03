package com.humg.HotelSystemManagement.entity.roomManagerment;

import com.humg.HotelSystemManagement.entity.booking.BookingRoom;
import com.humg.HotelSystemManagement.entity.enums.RoomStatus;
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
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_number", nullable = false, unique = true)
    String roomNumber;

//    @Column(name = "room_status", nullable = false)
//    //@Enumerated(EnumType.STRING)
//    String roomAvailability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    RoomType roomType;

//    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
//    List<BookingRoom> bookingRooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_room_id", nullable = false)
    BookingRoom bookingRoom;

//    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
//    List<RoomStatus> roomStatus = new ArrayList<>();

//    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//    RoomStatus roomStatus;

    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus;
}
