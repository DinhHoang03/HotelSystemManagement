package com.humg.HotelSystemManagement.service.HumanService;

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
public class CustomerService implements IGeneralHumanCRUDService<CustomerResponse, CustomerCreationRequest, CustomerUpdateRequest> {
    //Call Repository
    CustomerRepository customerRepository;
    SecurityConfig securityConfig;

    //Create customer account
    public CustomerResponse create(CustomerCreationRequest request) {
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
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        customer = customerRepository.save(customer);
        //Create a customer
        return CustomerResponse.builder()
                .identityId(customer.getIdentityId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .role(customer.getRole())
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
                        customer.getIdentityId(),
                        customer.getName(),
                        customer.getPhone(),
                        customer.getEmail(),
                        customer.getRole()

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
                .identityId(customer.getIdentityId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .role(customer.getRole())
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
                .identityId(updatedCustomer.getIdentityId())
                .name(updatedCustomer.getName())
                .email(updatedCustomer.getEmail())
                .phone(updatedCustomer.getPhone())
                .role(updatedCustomer.getRole())
                .build();

        return result;
    }

    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        customerRepository.delete(customer);
    }
}
