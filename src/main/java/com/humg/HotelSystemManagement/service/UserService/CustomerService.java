package com.humg.HotelSystemManagement.service.UserService;

import com.humg.HotelSystemManagement.configuration.security.SecurityConfig;
import com.humg.HotelSystemManagement.dto.request.user.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.user.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.user.customer.CustomerResponse;
import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import com.humg.HotelSystemManagement.entity.User.Customer;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.exceptions.AppException;
import com.humg.HotelSystemManagement.mapper.CustomerMapper;
import com.humg.HotelSystemManagement.repository.authenticationRepository.RoleRepository;
import com.humg.HotelSystemManagement.repository.User.CustomerRepository;
import com.humg.HotelSystemManagement.repository.User.EmployeeRepository;
import com.humg.HotelSystemManagement.utils.Interfaces.IGeneralCRUDService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService implements IGeneralCRUDService<CustomerResponse, CustomerCreationRequest, CustomerUpdateRequest, String> {
    //Call Repository
    CustomerRepository customerRepository;
    EmployeeRepository employeeRepository;
    RoleRepository roleRepository;
    CustomerMapper customerMapper;
    SecurityConfig securityConfig;

    //Create customer account
    //@PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponse create(CustomerCreationRequest request) {
        Customer customer;
        //Check if the email was registered with this customer account
        if (request != null) {
            
            if (customerRepository.existsByEmail(request.getEmail())
                    || customerRepository.existsByPhone(request.getPhone())
                    || employeeRepository.existsByUsername(request.getUsername())
            ) throw new AppException(AppErrorCode.USER_EXISTED);


            customer = customerMapper.toCustomer(request);
            //Mã hóa mật khẩu với thuật toán BCrypt
            String encodedPassword = securityConfig
                    .bcryptPasswordEncoder()
                    .encode(
                            request.getPassword()
                    );

            customer.setPassword(encodedPassword);
            var customerRole = roleRepository.findById("CUSTOMER")
                    .orElseGet(() -> roleRepository.save(new Role("CUSTOMER", "Customer Role", new HashSet<>())));

            customer.setRoles(new HashSet<>(Set.of(customerRole)));
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

    //Get All Sort by pages
    public Page<CustomerResponse> getAll(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerRepository.findAll(pageable);

        return customerPage.map(customerMapper::toCustomerResponse);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //Get User by Id
    public CustomerResponse getById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return customerMapper.toCustomerResponse(customer);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    //update User by Id
    public CustomerResponse update(String id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        if (request != null) {
            if (request.getName() != null && !request.getName().isEmpty()) {
                customer.setName(request.getName());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                customer.setEmail(request.getEmail());
            }
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                customer.setPhone(request.getPhone());
            }
            if (request.getAddress() != null && !request.getAddress().isEmpty()) {
                customer.setAddress(request.getAddress());
            }
        } else {
            throw new AppException(AppErrorCode.OBJECT_IS_NULL);
        }

        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public void delete(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        customerRepository.delete(customer);
    }
}
