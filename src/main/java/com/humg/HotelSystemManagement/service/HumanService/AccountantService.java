package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.accountant.AccountantCreationRequest;
import com.humg.HotelSystemManagement.dto.request.accountant.AccountantUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.accountant.AccountantResponse;
import com.humg.HotelSystemManagement.entity.employees.Accountant;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.employees.AccountantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountantService implements IGeneralHumanCRUDService<AccountantResponse, AccountantCreationRequest, AccountantUpdateRequest> {
    AccountantRepository accountantRepository;
    SecurityConfig securityConfig;

    public AccountantResponse create(AccountantCreationRequest request) {
        // Khai báo đúng kiểu Accountant thay vì Employee
        Accountant accountant;

        if (request != null) {
            if (accountantRepository.existsByEmail(request.getEmail()) ||
                    accountantRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            accountant = Accountant.builder()
                    .name(request.getName())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role("ACCOUNTANT")
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        accountant = accountantRepository.save(accountant);

        return AccountantResponse.builder()
                .id(accountant.getId()) // Thay getAccountantId() bằng getId()
                .name(accountant.getName())
                .phone(accountant.getPhone())
                .email(accountant.getEmail())
                .role(accountant.getRole())
                .build();
    }

    public List<AccountantResponse> getAll() {
        List<AccountantResponse> list = accountantRepository.findAll()
                .stream()
                .map(accountant -> new AccountantResponse(
                        accountant.getId(), // Thay getAccountantId() bằng getId()
                        accountant.getName(),
                        accountant.getEmail(),
                        accountant.getPhone(),
                        accountant.getRole()
                ))
                .toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public AccountantResponse getById(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return AccountantResponse.builder()
                .id(accountant.getId()) // Thay getAccountantId() bằng getId()
                .name(accountant.getName())
                .phone(accountant.getPhone())
                .email(accountant.getEmail())
                .role(accountant.getRole())
                .build();
    }

    public AccountantResponse updateById(Long id, AccountantUpdateRequest request) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            accountant.setEmail(request.getEmail());
            accountant.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Accountant updatedAccountant = accountantRepository.save(accountant);

        return AccountantResponse.builder()
                .id(updatedAccountant.getId()) // Thay getAccountantId() bằng getId()
                .name(updatedAccountant.getName())
                .email(updatedAccountant.getEmail())
                .phone(updatedAccountant.getPhone())
                .role(updatedAccountant.getRole())
                .build();
    }

    public void deleteById(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        accountantRepository.delete(accountant);
    }
}