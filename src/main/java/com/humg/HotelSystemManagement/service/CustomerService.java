package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.customer.CustomerResponse;
import com.humg.HotelSystemManagement.entity.booking.Customer;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.repository.booking.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {
    //Call Repository
    CustomerRepository customerRepository;
    SecurityConfig securityConfig;

    //Create customer account
    public Customer createCustomer(CustomerCreationRequest request) {
        Customer customer;
        //Check if the email was registered with this customer account
        if (request != null) {

            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            //Mã hóa mật khẩu với thuật toán BCrypt
            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            customer = Customer.builder()
                    .identityId(request.getIdentityId())
                    .name(request.getName())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .build();
        }else {
            throw new AppException(AppErrorCode.REQUEST_NULL);
        }
        //Create a customer
        return customerRepository.save(customer);
    }

    //Get all customers account
    public List<CustomerResponse> getAllUSers() {
        //Tạo list để luu trữ list dữ liệu, gọi service để lấy hàm findAll lấy toàn bộ dũ liệu của user
        List<CustomerResponse> list = customerRepository.findAll()
                //Chuyển từ list thành một stream(Luồng dũ liệu)
                .stream()
                //Dùng map để chuyển từng Customer thành CustomerResponse
                .map(customer -> new CustomerResponse(
                        //Lấy nhũng dữ liệu cần thiết của entity Customer
                        customer.getCustomerId(),
                        customer.getIdentityId(),
                        customer.getName(),
                        customer.getPhone(),
                        customer.getEmail()

                )).toList();//Chuyển tù luồng dũ liệu(stream) thành một list

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    //Get User by Id
    public CustomerResponse findUserById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        CustomerResponse response = CustomerResponse.builder()
                .identityId(customer.getIdentityId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();

        return response;
    }

    //update User by Id
    public CustomerResponse updateUserById(Long id, CustomerUpdateRequest request) {
        Customer customerRequest = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (customerRequest != null) {
            customerRequest.setEmail(request.getEmail());
            customerRequest.setPhone(request.getPhone());
        } else {
            throw new AppException(AppErrorCode.REQUEST_NULL);
        }

        Customer updatedCustomer = customerRepository.save(customerRequest);

        CustomerResponse result = CustomerResponse.builder()
                .customerId(updatedCustomer.getCustomerId())
                .identityId(updatedCustomer.getIdentityId())
                .name(updatedCustomer.getName())
                .email(updatedCustomer.getEmail())
                .phone(updatedCustomer.getPhone())
                .build();

        return result;
    }

    public void deleteUserById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        customerRepository.delete(customer);
    }
}
