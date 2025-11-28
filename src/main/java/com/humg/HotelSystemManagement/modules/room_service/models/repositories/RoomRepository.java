package com.humg.HotelSystemManagement.modules.room_service.models.repositories;

import com.humg.HotelSystemManagement.utils.enums.RoomStatus;
import com.humg.HotelSystemManagement.modules.room_service.models.entities.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findAllByRoomNumberIn(List<String> roomNumbers);

    @Query("SELECT r FROM Room r WHERE LOWER(r.roomType.roomTypes) LIKE LOWER(CONCAT('%', :typeName, '%'))")
    Page<Room> findByRoomTypeNameContaining(@Param("typeName") String typeName, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();
}
