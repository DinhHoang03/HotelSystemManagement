package com.humg.HotelSystemManagement.service.SystemServices.booking;

import com.humg.HotelSystemManagement.repository.booking.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService{
    BookingRepository bookingRepository;

}
