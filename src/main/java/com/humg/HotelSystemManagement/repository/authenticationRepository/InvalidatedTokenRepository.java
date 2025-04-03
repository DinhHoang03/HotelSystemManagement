package com.humg.HotelSystemManagement.repository.authenticationRepository;

import com.humg.HotelSystemManagement.entity.authorizezation.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Modifying
    @Query("DELETE FROM InvalidatedToken t WHERE t.expiredTime < :currentTime")
    int deleteByExpiredTimeBefore(@Param("currentTime") Instant currentTime);
}
