package com.humg.HotelSystemManagement.modules.customer_service.mappers;

import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.CustomerCreationRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.requests.CustomerUpdateRequest;
import com.humg.HotelSystemManagement.modules.customer_service.resources.responses.CustomerResponse;
import com.humg.HotelSystemManagement.modules.customer_service.models.entities.Customer;
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
