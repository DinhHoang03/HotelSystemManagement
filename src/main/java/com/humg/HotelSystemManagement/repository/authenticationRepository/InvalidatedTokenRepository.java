package com.humg.HotelSystemManagement.repository.authenticationRepository;

import com.humg.HotelSystemManagement.entity.authorizezation.InvalidatedToken;
import com.humg.HotelSystemManagement.entity.authorizezation.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

}
