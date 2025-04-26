package com.humg.HotelSystemManagement.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {
    RedisTemplate<String, Object> redisTemplate;

    /**
     * Blacklist a token by storing it in Redis with a specified expiration time.
     *
     * @param tokenId           The ID of the token to be blacklisted.
     * @param durationInSeconds The duration in seconds for which the token should be blacklisted.
     */
    public void blacklistToken(String tokenId, long durationInSeconds) {
        String key = "blacklist:" + tokenId;
        String value = "true";
        Duration timeToLive = Duration.ofSeconds(durationInSeconds);

        redisTemplate.opsForValue().set(key, value, timeToLive);
    }

    /**
     * Check if a token is blacklisted in Redis.
     *
     * @param tokenId The ID of the token to check.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String tokenId) {
        return redisTemplate.hasKey("blacklist:" + tokenId);
    }
}
