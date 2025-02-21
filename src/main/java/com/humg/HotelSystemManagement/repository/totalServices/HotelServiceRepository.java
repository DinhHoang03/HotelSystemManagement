package com.humg.HotelSystemManagement.repository.totalServices;

import com.humg.HotelSystemManagement.entity.totalServices.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {
}
