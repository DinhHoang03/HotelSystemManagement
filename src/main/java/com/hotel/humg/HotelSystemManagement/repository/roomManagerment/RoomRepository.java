package com.hotel.humg.HotelSystemManagement.repository.roomManagerment;

import com.hotel.humg.HotelSystemManagement.entity.enums.RoomStatus;
import com.hotel.humg.HotelSystemManagement.entity.roomManagerment.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findAllByRoomNumberIn(List<String> roomNumbers);

    List<Room> findByRoomStatus(RoomStatus roomStatus);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomStatus = 'OCCUPIED'")
    long countOccupiedRooms();

    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();
}
