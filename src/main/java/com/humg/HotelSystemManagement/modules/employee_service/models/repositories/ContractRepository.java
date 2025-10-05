package com.humg.HotelSystemManagement.modules.employee_service.models.repositories;

import com.humg.HotelSystemManagement.modules.employee_service.models.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

}
