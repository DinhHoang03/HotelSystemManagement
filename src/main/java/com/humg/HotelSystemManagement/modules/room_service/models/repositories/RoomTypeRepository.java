package com.humg.HotelSystemManagement.modules.room_service.models.repositories;

import com.humg.HotelSystemManagement.modules.room_service.models.entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByRoomTypes(String roomType);
    Optional<RoomType> findByRoomTypes(String roomTypes);
}
