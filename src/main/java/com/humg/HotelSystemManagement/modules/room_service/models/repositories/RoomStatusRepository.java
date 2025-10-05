package com.humg.HotelSystemManagement.modules.room_service.models.repositories;

import com.humg.HotelSystemManagement.modules.room_service.models.entities.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomStatusRepository extends JpaRepository<RoomStatus, Long> {
    boolean existsByRoomStatus(String roomStatus);
    Optional<RoomStatus> findByRoomStatus(String roomStatus);
}
