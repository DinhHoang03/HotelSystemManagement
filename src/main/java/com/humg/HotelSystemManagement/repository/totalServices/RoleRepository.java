package com.humg.HotelSystemManagement.repository.totalServices;

import com.humg.HotelSystemManagement.entity.authorizezation.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

}
