package com.humg.HotelSystemManagement.utils;

import com.humg.HotelSystemManagement.repository.authenticationRepository.InvalidatedTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenCleanupService {
    
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 milliseconds)
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup task");
        
        // Get current time
        Instant now = Instant.now();
        
        // Delete all tokens that have expired
        int deletedCount = invalidatedTokenRepository.deleteByExpiredTimeBefore(now);
        
        log.info("Token cleanup completed. Deleted {} expired tokens", deletedCount);
    }
} 