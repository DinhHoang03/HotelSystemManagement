package com.humg.HotelSystemManagement.service.SystemService;

import com.humg.HotelSystemManagement.repository.booking.BookingItemsRepository;
import com.humg.HotelSystemManagement.repository.booking.BookingRoomRepository;
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
public class CleanBookingDump {
    BookingItemsRepository bookingItemsRepository;
    BookingRoomRepository bookingRoomRepository;

    @Scheduled(fixedRate = 3600000)
    public void cleanUpBooking() {
        long startTime = System.currentTimeMillis();
        log.info("Starting clean up task at {}", Instant.now());

        int deleteBookingRoomCount = cleanUpBookingRoom();
        int deleteBookingItemsCount = cleanUpBookingItems();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Clean up completed at {}. Deleted {} BookingRoom and {} BookingItems in {} ms",
                Instant.now(), deleteBookingRoomCount, deleteBookingItemsCount, duration);
    }

    @Transactional
    private int cleanUpBookingRoom() {
        try {
            int deletedCount = bookingRoomRepository.deleteByBookingIsNull();
            log.debug("Deleted {} BookingRoom records", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to delete BookingRoom records: {}", e.getMessage(), e);
            return 0; // Hoặc throw exception nếu muốn dừng task
        }
    }

    @Transactional
    private int cleanUpBookingItems() {
        try {
            int deletedCount = bookingItemsRepository.deleteByBookingIsNull();
            log.debug("Deleted {} BookingItems records", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to delete BookingItems records: {}", e.getMessage(), e);
            return 0; // Hoặc throw exception nếu muốn dừng task
        }
    }
}