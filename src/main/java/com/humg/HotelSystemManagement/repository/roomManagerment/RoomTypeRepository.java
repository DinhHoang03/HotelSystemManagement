package com.humg.HotelSystemManagement.repository.roomManagerment;

import com.humg.HotelSystemManagement.entity.roomManagerment.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

}
