package com.humg.HotelSystemManagement.mapper;

import com.humg.HotelSystemManagement.dto.request.customer.CustomerCreationRequest;
import com.humg.HotelSystemManagement.dto.request.customer.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.dto.response.humanEntity.customer.CustomerResponse;
import com.humg.HotelSystemManagement.entity.humanEntity.Customer;
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
