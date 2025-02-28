package com.humg.HotelSystemManagement.service.HumanService;

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
public class CleanerService implements IGeneralHumanCRUDService<CleanerResponse, CleanerCreationRequest, CleanerUpdateRequest> {
    CleanerRepository cleanerRepository;
    SecurityConfig securityConfig;

    public CleanerResponse create(CleanerCreationRequest request) {
        Cleaner cleaner;

        if (request != null) {

            if (cleanerRepository.existsByEmail(request.getEmail()) ||
                    cleanerRepository.existsByPhone(request.getPhone())) {
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
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        cleaner = cleanerRepository.save(cleaner);

        return CleanerResponse.builder()
                .cleanerId(cleaner.getId())
                .name(cleaner.getName())
                .email(cleaner.getEmail())
                .phone(cleaner.getPhone())
                .build();
    }

    public List<CleanerResponse> getAll() {
        List<CleanerResponse> list = cleanerRepository.findAll()
                .stream()
                .map(cleaner -> new CleanerResponse(
                        cleaner.getId(),
                        cleaner.getName(),
                        cleaner.getEmail(),
                        cleaner.getPhone()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }
        return list;
    }

    public CleanerResponse getById(Long id) {
        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        CleanerResponse response = CleanerResponse.builder()
                .cleanerId(cleaner.getId())
                .name(cleaner.getName())
                .email(cleaner.getEmail())
                .phone(cleaner.getPhone())
                .build();

        return response;
    }

    public CleanerResponse updateById(Long id, CleanerUpdateRequest request) {

        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            cleaner.setEmail(request.getEmail());
            cleaner.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Cleaner updatedCleaner = cleanerRepository.save(cleaner);

        CleanerResponse response = CleanerResponse.builder()
                .cleanerId(updatedCleaner.getId())
                .name(updatedCleaner.getName())
                .phone(updatedCleaner.getPhone())
                .email(updatedCleaner.getEmail())
                .build();

        return response;
    }

    public void deleteById(Long id) {
        Cleaner cleaner = cleanerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        cleanerRepository.delete(cleaner);
    }
}
