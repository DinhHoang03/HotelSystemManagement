package com.humg.HotelSystemManagement.service.HumanService;

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
public class ReceptionistService implements IGeneralHumanCRUDService<ReceptionistResponse, ReceptionistCreationRequest, ReceptionistUpdateRequest> {

    ReceptionistRepository receptionistRepository;
    SecurityConfig securityConfig;

    public ReceptionistResponse create(ReceptionistCreationRequest request) {
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
                    .role("RECEPTIONIST")
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        receptionist = receptionistRepository.save(receptionist);

        return ReceptionistResponse.builder()
                .id(receptionist.getId())
                .name(receptionist.getName())
                .email(receptionist.getEmail())
                .phone(receptionist.getPhone())
                .role(receptionist.getRole())
                .build();
    }

    public List<ReceptionistResponse> getAll() {
        List<ReceptionistResponse> list = receptionistRepository.findAll()
                .stream()
                .map(receptionist -> new ReceptionistResponse(
                        receptionist.getId(),
                        receptionist.getName(),
                        receptionist.getEmail(),
                        receptionist.getPhone(),
                        receptionist.getRole()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public ReceptionistResponse getById(Long id){
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        ReceptionistResponse response = ReceptionistResponse.builder()
                .id(receptionist.getId())
                .name(receptionist.getName())
                .email(receptionist.getEmail())
                .phone(receptionist.getPhone())
                .role(receptionist.getRole())
                .build();

        return response;
    }

    public ReceptionistResponse updateById(Long id, ReceptionistUpdateRequest request){
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
                .id(updatedReceptionist.getId())
                .name(updatedReceptionist.getName())
                .email(updatedReceptionist.getEmail())
                .phone(updatedReceptionist.getPhone())
                .role(updatedReceptionist.getRole())
                .build();

        return result;
    }

    public void deleteById(Long id){
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_EXISTED));

        receptionistRepository.delete(receptionist);
    }
}
