package com.hotel.humg.HotelSystemManagement.mapper;

import com.hotel.humg.HotelSystemManagement.dto.request.user.customer.CustomerCreationRequest;
import com.hotel.humg.HotelSystemManagement.dto.request.user.customer.CustomerUpdateRequest;
import com.hotel.humg.HotelSystemManagement.dto.response.user.customer.CustomerResponse;
import com.hotel.humg.HotelSystemManagement.entity.User.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "password", ignore = true)
    Customer toCustomer(CustomerCreationRequest request);

    CustomerResponse toCustomerResponse(Customer customer);

    void updateCustomer(@MappingTarget Customer customer, CustomerUpdateRequest request);
}
