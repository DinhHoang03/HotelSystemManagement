package com.humg.HotelSystemManagement.repository.totalServices;

import com.humg.HotelSystemManagement.entity.authorizezation.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

}
