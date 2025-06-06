package com.humg.HotelSystemManagement.service.HumanService;

import com.humg.HotelSystemManagement.configuration.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.customer.CustomerResponse;
import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.CustomerMapper;
import com.humg.HotelSystemManagement.repository.humanEntity.CustomerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService implements IGeneralHumanCRUDService<CustomerResponse, CustomerCreationRequest, CustomerUpdateRequest> {
    //Call Repository
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    SecurityConfig securityConfig;

    //Create customer account
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse create(CustomerCreationRequest request) {
        Customer customer;
        //Check if the email was registered with this customer account
        if (request != null) {

            if (customerRepository.existsByEmail(request.getEmail()) ||
                    customerRepository.existsByPhone(request.getPhone())) {
                throw new AppException(AppErrorCode.USER_EXISTED);
            }

            customer = customerMapper.toCustomer(request);
            //Mã hóa mật khẩu với thuật toán BCrypt
            String encodedPassword = securityConfig.bcryptPasswordEncoder().encode(request.getPassword());

            customer.setPassword(encodedPassword);
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        customer = customerRepository.save(customer);
        //Create a customer
        return customerMapper.toCustomerResponse(customer);
    }

    public CustomerResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return customerMapper.toCustomerResponse(customer);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAuthority('GET_ALL_EMPLOYEE')")
    //Get all customers account
    public List<CustomerResponse> getAll() {
        //Tạo list để luu trữ list dữ liệu, gọi service để lấy hàm findAll lấy toàn bộ dũ liệu của user
        List<CustomerResponse> list = customerRepository.findAll()
                //Chuyển từ list thành một stream(Luồng dũ liệu)
                .stream()
                //Dùng map để chuyển từng Customer thành CustomerResponse
                .map(customerMapper::toCustomerResponse)//Lấy nhũng dữ liệu cần thiết của entity Customer
                .toList();//Chuyển tù luồng dũ liệu(stream) thành một list

        if (list.isEmpty()) {
            throw new AppException(AppErrorCode.LIST_EMPTY);
        }

        return list;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //Get User by Id
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return customerMapper.toCustomerResponse(customer);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
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

        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public void deleteById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        customerRepository.delete(customer);
    }
}
