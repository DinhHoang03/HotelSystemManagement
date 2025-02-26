package com.humg.HotelSystemManagement.service.MainEntitiesService;

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
public class AccountantService {
    AccountantRepository accountantRepository;
    SecurityConfig securityConfig;

    public Accountant createAccountant(AccountantCreationRequest request) {
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
                    .build();

        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        return accountantRepository.save(accountant);
    }

    public List<AccountantResponse> getAllAccountants() {
        List<AccountantResponse> list = accountantRepository.findAll()
                .stream()
                .map(accountant -> new AccountantResponse(
                        accountant.getAccountantId(),
                        accountant.getName(),
                        accountant.getEmail(),
                        accountant.getPhone()
                )).toList();

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    public AccountantResponse findAccountantById(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        AccountantResponse response = AccountantResponse.builder()
                .accountantId(accountant.getAccountantId())
                .name(accountant.getName())
                .phone(accountant.getPhone())
                .email(accountant.getEmail())
                .build();

        return response;
    }

    public AccountantResponse updateAccountantResponse(Long id, AccountantUpdateRequest request) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            accountant.setEmail(request.getEmail());
            accountant.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Accountant updatedAccountant = accountantRepository.save(accountant);

        AccountantResponse result = AccountantResponse.builder()
                .accountantId(updatedAccountant.getAccountantId())
                .name(updatedAccountant.getName())
                .email(updatedAccountant.getEmail())
                .phone(updatedAccountant.getPhone())
                .build();

        return result;
    }

    public void deleteAccountantById(Long id) {
        Accountant accountant = accountantRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        accountantRepository.delete(accountant);
    }
}
