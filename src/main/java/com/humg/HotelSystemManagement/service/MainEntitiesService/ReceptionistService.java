package com.humg.HotelSystemManagement.service.MainEntitiesService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.receptionist.ReceptionistCreationRequest;
import com.humg.HotelSystemManagement.dto.request.receptionist.ReceptionistUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.receptionist.ReceptionistResponse;
import com.humg.HotelSystemManagement.entity.employees.Receptionist;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.ReceptionistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReceptionistService {

    ReceptionistRepository receptionistRepository;
    SecurityConfig securityConfig;

    public Receptionist createReceptionist(ReceptionistCreationRequest request) {
        Receptionist receptionist;

        if (request != null) {

            if (receptionistRepository.existsByEmail(request.getEmail()) ||
                    receptionistRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            receptionist = Receptionist.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .password(encodedPassword)
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }
        return receptionistRepository.save(receptionist);
    }

    public List<ReceptionistResponse> getAllReceptionists() {
        List<ReceptionistResponse> list = receptionistRepository.findAll()
                .stream()
                .map(receptionist -> new ReceptionistResponse(
                        receptionist.getReceptionistId(),
                        receptionist.getName(),
                        receptionist.getEmail(),
                        receptionist.getPhone()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public ReceptionistResponse findReceptionistById(Long id){
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        ReceptionistResponse response = ReceptionistResponse.builder()
                .receptionistId(receptionist.getReceptionistId())
                .name(receptionist.getName())
                .email(receptionist.getEmail())
                .phone(receptionist.getPhone())
                .build();

        return response;
    }

    public ReceptionistResponse updateReceptionist(Long id, ReceptionistUpdateRequest request){
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_EXISTED));

        if(request != null){
            receptionist.setEmail(request.getEmail());
            receptionist.setPhone(request.getPhone());
        }else{
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Receptionist updatedReceptionist = receptionistRepository.save(receptionist);

        ReceptionistResponse result = ReceptionistResponse.builder()
                .receptionistId(updatedReceptionist.getReceptionistId())
                .name(updatedReceptionist.getName())
                .email(updatedReceptionist.getEmail())
                .phone(updatedReceptionist.getPhone())
                .build();

        return result;
    }

    public void deleteReceptionistById(Long id){
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_EXISTED));

        receptionistRepository.delete(receptionist);
    }
}
