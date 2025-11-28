package com.humg.HotelSystemManagement.modules.booking_service.models.repositories;

import com.humg.HotelSystemManagement.modules.booking_service.models.entities.BookingItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemsRepository extends JpaRepository<BookingItems, String> {
    Page<BookingItems> findByUsername(String username, Pageable pageable);
    List<BookingItems> findByUsernameAndBookingItemIdIn(String username, List<String> bookingItemIds);
}