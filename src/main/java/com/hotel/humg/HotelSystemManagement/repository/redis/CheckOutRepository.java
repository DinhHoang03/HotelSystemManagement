package com.hotel.humg.HotelSystemManagement.repository.redis;

import com.hotel.humg.HotelSystemManagement.redis.CheckOutCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckOutRepository extends CrudRepository<CheckOutCache, String> {
}
