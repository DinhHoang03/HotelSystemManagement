package com.humg.HotelSystemManagement.repository.roomManagerment;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByRoomTypes(String roomType);
    Optional<RoomType> findByRoomTypes(String roomTypes);
}
