package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.cleaner.CleanerCreationRequest;
import com.humg.HotelSystemManagement.dto.response.cleaner.CleanerResponse;
import com.humg.HotelSystemManagement.entity.employees.Admin;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CleanerService {
    CleanerRepository cleanerRepository;
    SecurityConfig securityConfig;

    public Cleaner createCleaner(CleanerCreationRequest request) {
        Cleaner cleaner;

        if (cleanerRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AppErrorCode.USER_EXISTED);
        }

        String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

        if (request != null) {
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

    public List<CleanerResponse> getAllCleaners(){
        List<CleanerResponse> list = cleanerRepository.findAll().stream().map(cleaner -> new CleanerResponse(
                cleaner.getCleanerId(),
                cleaner.getName(),
                cleaner.getEmail(),
                cleaner.getPhone()
        )).toList();

        if(list == null){
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    public CleanerResponse findCleanerById(Long id){
        Cleaner cleaner = cleanerRepository.findById(id).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return null;
    }
}
