package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.dto.request.waiter.WaiterCreationRequest;
import com.humg.HotelSystemManagement.dto.request.waiter.WaiterUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.waiter.WaiterResponse;
import com.humg.HotelSystemManagement.entity.employees.Waiter;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.CustomerRepository;
import com.humg.HotelSystemManagement.repository.employees.WaiterRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaiterService {

    WaiterRepository waiterRepository;
    private final CustomerRepository customerRepository;

    public Waiter createWaiter(WaiterCreationRequest request) {
        if (waiterRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }

        return waiterRepository.save(Waiter.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .build());
    }

    public List<WaiterResponse> getAllWaiters() {
        List<WaiterResponse> list = waiterRepository.findAll()
                .stream()
                .map(waiter -> new WaiterResponse(
                        waiter.getName(),
                        waiter.getEmail(),
                        waiter.getPhone()
                )).toList();

        if (list == null) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public WaiterResponse findWaiterById(Long id) {
        Waiter waiter = waiterRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return WaiterResponse.builder()
                .name(waiter.getName())
                .phone(waiter.getPhone())
                .email(waiter.getEmail())
                .build();
    }

    public WaiterResponse updateUserById(Long id, WaiterUpdateRequest request){
        Waiter waiter = waiterRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));


        return null;
    }
}
