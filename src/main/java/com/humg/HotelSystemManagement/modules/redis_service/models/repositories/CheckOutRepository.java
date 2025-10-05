package com.humg.HotelSystemManagement.modules.redis_service.models.repositories;

import com.humg.HotelSystemManagement.modules.redis_service.models.entities.CheckOutCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckOutRepository extends CrudRepository<CheckOutCache, String> {
}
