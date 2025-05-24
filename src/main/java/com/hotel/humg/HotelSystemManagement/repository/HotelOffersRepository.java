package com.hotel.humg.HotelSystemManagement.repository;

import com.hotel.humg.HotelSystemManagement.entity.totalServices.HotelOffers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelOffersRepository extends JpaRepository<HotelOffers, String> {

    /**
    @Query(value = "SELECT * FROM hotel_service WHERE service_type = :serviceType", nativeQuery = true)
    Optional<HotelOffers> findServiceType(@Param(value = "serviceType") String serviceType);

    @Query(value = "SELECT * FROM hotel_service WHERE service_type = :serviceType", nativeQuery = true)
    boolean existsByServiceTypes(@Param(value = "serviceType") String serviceType);
    */

    Optional<HotelOffers> findByServiceTypes(String serviceType);
    boolean existsByServiceTypes(String serviceType);
}
