package com.humg.HotelSystemManagement.modules.redis_service.models.repositories;

import com.humg.HotelSystemManagement.modules.redis_service.models.entities.CheckInCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends CrudRepository<CheckInCache, String> {
}
