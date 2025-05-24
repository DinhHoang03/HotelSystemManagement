package com.hotel.humg.HotelSystemManagement.repository.staffManagerment;

import com.hotel.humg.HotelSystemManagement.entity.staffManagerment.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

}
