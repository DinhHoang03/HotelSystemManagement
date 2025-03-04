package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.customer.CustomerResponse;
import com.humg.HotelSystemManagement.entity.enums.Gender;
import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService implements IGeneralHumanCRUDService<CustomerResponse, CustomerCreationRequest, CustomerUpdateRequest> {
    //Call Repository
    CustomerRepository customerRepository;
    SecurityConfig securityConfig;

    //Create customer account
    public CustomerResponse create(CustomerCreationRequest request) {
        Customer customer;
        //Check if the email was registered with this customer account
        if (request != null) {

            if (customerRepository.existsByEmail(request.getEmail()) ||
                    customerRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            //Mã hóa mật khẩu với thuật toán BCrypt
            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            customer = Customer.builder()
                    .identityId(request.getIdentityId())
                    .name(request.getName())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .dob(request.getDob())
                    .gender(Gender.valueOf(request.getGender()))
                    .address(request.getAddress())
                    .password(encodedPassword)
                    .role("CUSTOMER")
                    .build();
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        customer = customerRepository.save(customer);
        //Create a customer
        return CustomerResponse.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .name(customer.getName())
                .gender(customer.getGender().toString())
                .dob(customer.getDob())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .identityId(customer.getIdentityId())
                .role(customer.getRole())
                .address(customer.getAddress())
                .build();
    }

    //Get all customers account
    public List<CustomerResponse> getAll() {
        //Tạo list để luu trữ list dữ liệu, gọi service để lấy hàm findAll lấy toàn bộ dũ liệu của user
        List<CustomerResponse> list = customerRepository.findAll()
                //Chuyển từ list thành một stream(Luồng dũ liệu)
                .stream()
                //Dùng map để chuyển từng Customer thành CustomerResponse
                .map(customer -> new CustomerResponse(
                        //Lấy nhũng dữ liệu cần thiết của entity Customer
                        customer.getId(),
                        customer.getUsername(),
                        customer.getName(),
                        customer.getGender().toString(),
                        customer.getDob(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getIdentityId(),
                        customer.getRole(),
                        customer.getAddress()
                )).toList();//Chuyển tù luồng dũ liệu(stream) thành một list

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    //Get User by Id
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        CustomerResponse response = CustomerResponse.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .name(customer.getName())
                .gender(customer.getGender().toString())
                .dob(customer.getDob())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .identityId(customer.getIdentityId())
                .role(customer.getRole())
                .address(customer.getAddress())
                .build();

        return response;
    }

    //update User by Id
    public CustomerResponse updateById(Long id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Customer updatedCustomer = customerRepository.save(customer);

        CustomerResponse result = CustomerResponse.builder()
                .id(updatedCustomer.getId())
                .username(updatedCustomer.getUsername())
                .name(updatedCustomer.getName())
                .gender(updatedCustomer.getGender().toString())
                .dob(updatedCustomer.getDob())
                .email(updatedCustomer.getEmail())
                .phone(updatedCustomer.getPhone())
                .identityId(updatedCustomer.getIdentityId())
                .role(updatedCustomer.getRole())
                .address(updatedCustomer.getAddress())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        customerRepository.delete(customer);
    }
}
