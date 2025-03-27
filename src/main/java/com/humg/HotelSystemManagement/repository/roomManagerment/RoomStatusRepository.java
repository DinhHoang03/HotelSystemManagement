package com.humg.HotelSystemManagement.repository.roomManagerment;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomStatusRepository extends JpaRepository<RoomStatus, Long> {
    boolean existsByRoomStatus(String roomStatus);
    Optional<RoomStatus> findByRoomStatus(String roomStatus);
}
