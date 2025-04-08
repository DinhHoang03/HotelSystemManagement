package com.humg.HotelSystemManagement.service.HotelService.booking;

import com.humg.HotelSystemManagement.repository.booking.BookingBillRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingBillService {
    BookingBillRepository bookingBillRepository;


}
