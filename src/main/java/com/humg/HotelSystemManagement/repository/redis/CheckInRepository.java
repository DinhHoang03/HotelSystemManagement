package com.humg.HotelSystemManagement.repository.redis;

import com.humg.HotelSystemManagement.redis.CheckInCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends CrudRepository<CheckInCache, String> {
}
