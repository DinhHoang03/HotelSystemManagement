package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.repository.employees.WaiterRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaiterService {

    WaiterRepository waiterRepository;

    public Waiter createWaiter(){

    }
}
