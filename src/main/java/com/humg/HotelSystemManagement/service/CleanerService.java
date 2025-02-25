package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.cleaner.CleanerResponse;
import com.humg.HotelSystemManagement.entity.employees.Cleaner;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.CleanerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CleanerService {
    CleanerRepository cleanerRepository;
    SecurityConfig securityConfig;

    public Cleaner createCleaner(CleanerCreationRequest request) {
        Cleaner cleaner;

        if (request != null) {
            if (cleanerRepository.existsByEmail(request.getEmail())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            cleaner = Cleaner.builder()
                    .name(request.getName())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .build();
        } else {
            throw new AppException(AppErrorCode.REQUEST_NULL);
        }

        return cleanerRepository.save(cleaner);
    }

    public List<CleanerResponse> getAllCleaners() {
        List<CleanerResponse> list = cleanerRepository.findAll()
                .stream()
                .map(cleaner -> new CleanerResponse(
                        cleaner.getCleanerId(),
                        cleaner.getName(),
                        cleaner.getEmail(),
                        cleaner.getPhone()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    public CleanerResponse findCleanerById(Long id) {
        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        CleanerResponse response = CleanerResponse.builder()
                .cleanerId(cleaner.getCleanerId())
                .name(cleaner.getName())
                .email(cleaner.getEmail())
                .phone(cleaner.getPhone())
                .build();

        return response;
    }

    public CleanerResponse updateCleaner(Long id, CleanerUpdateRequest request) {

        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            cleaner.setEmail(request.getEmail());
            cleaner.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.REQUEST_NULL);
        }

        Cleaner updatedCleaner = cleanerRepository.save(cleaner);

        CleanerResponse response = CleanerResponse.builder()
                .cleanerId(updatedCleaner.getCleanerId())
                .name(updatedCleaner.getName())
                .phone(updatedCleaner.getPhone())
                .email(updatedCleaner.getEmail())
                .build();

        return response;
    }

    public void deleteCleanerById(Long id) {
        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        cleanerRepository.delete(cleaner);
    }
}
