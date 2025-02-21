package com.humg.HotelSystemManagement.service;

import com.humg.HotelSystemManagement.dto.request.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.response.CustomerResponse;
import com.humg.HotelSystemManagement.entity.booking.Customer;
import com.humg.HotelSystemManagement.exception.enums.AppErrorCode;
import com.humg.HotelSystemManagement.exception.enums.booking.CustomerErrorCode;
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

    //Create customer account
    public Customer createCustomer(CustomerCreationRequest request){
        //Check if the email was registered with this customer account
        if(customerRepository.existsByEmail(request.getEmail())){
            throw new AppException(AppErrorCode.USER_EXISTED);
        }
        //Create a customer
        Customer customer = customerRepository.save(Customer.builder()
                .identityId(request.getIdentityId())
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(request.getPassword())
                .build());
        return customer;
    }

    //Get all customers account
    public List<Customer> getAllUSers(){
        return customerRepository.findAll();
    }

    //Get User by Id
    public CustomerResponse findUserById(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_EXISTED));

        return CustomerResponse.builder()
                .identityId(customer.getIdentityId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone()).build();
    }
}
