package com.hotel.humg.HotelSystemManagement.repository.authenticationRepository;

import com.hotel.humg.HotelSystemManagement.entity.authorizezation.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

}
