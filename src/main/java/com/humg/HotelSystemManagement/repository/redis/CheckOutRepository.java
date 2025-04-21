package com.humg.HotelSystemManagement.repository.redis;

import com.humg.HotelSystemManagement.redis.CheckOutCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckOutRepository extends CrudRepository<CheckOutCache, String> {
}
