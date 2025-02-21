package com.humg.HotelSystemManagement.repository.roomManagerment;

import com.humg.HotelSystemManagement.entity.roomManagerment.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
