package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.waiter.WaiterCreationRequest;
import com.humg.HotelSystemManagement.dto.request.waiter.WaiterUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.waiter.WaiterResponse;
import com.humg.HotelSystemManagement.entity.employees.Waiter;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.WaiterRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaiterService implements IGeneralHumanCRUDService<WaiterResponse, WaiterCreationRequest, WaiterUpdateRequest> {

    WaiterRepository waiterRepository;
    SecurityConfig securityConfig;

    public WaiterResponse create(WaiterCreationRequest request) {
        Waiter waiter;

        if (request != null) {

            if (waiterRepository.existsByEmail(request.getEmail()) ||
                    waiterRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            waiter = Waiter.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .password(encodedPassword)
                    .role("WAITER")
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        waiter = waiterRepository.save(waiter);

        return WaiterResponse.builder()
                .name(waiter.getName())
                .phone(waiter.getPhone())
                .email(waiter.getEmail())
                .role(waiter.getRole())
                .build();
    }

    public List<WaiterResponse> getAll() {
        List<WaiterResponse> list = waiterRepository.findAll()
                .stream()
                .map(waiter -> new WaiterResponse(
                        waiter.getId(),
                        waiter.getName(),
                        waiter.getEmail(),
                        waiter.getPhone(),
                        waiter.getRole()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    public WaiterResponse getById(Long id) {
        Waiter waiter = waiterRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        WaiterResponse response = WaiterResponse.builder()
                .name(waiter.getName())
                .phone(waiter.getPhone())
                .email(waiter.getEmail())
                .role(waiter.getRole())
                .build();

        return response;
    }

    public WaiterResponse updateById(Long id, WaiterUpdateRequest request) {
        Waiter waiter = waiterRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            waiter.setEmail(request.getEmail());
            waiter.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Waiter updatedWaiter = waiterRepository.save(waiter);

        WaiterResponse result = WaiterResponse.builder()
                .id(updatedWaiter.getId())
                .name(updatedWaiter.getName())
                .phone(updatedWaiter.getPhone())
                .email(updatedWaiter.getEmail())
                .role(updatedWaiter.getRole())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Waiter waiter = waiterRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        waiterRepository.delete(waiter);
    }
}
