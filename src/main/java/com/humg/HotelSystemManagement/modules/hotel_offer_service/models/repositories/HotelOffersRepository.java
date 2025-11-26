package com.humg.HotelSystemManagement.modules.hotel_offer_service.models.repositories;

import com.humg.HotelSystemManagement.modules.hotel_offer_service.models.entities.HotelOffers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelOffersRepository extends JpaRepository<HotelOffers, String> {

    List<HotelOffers> findByServiceCategory(String serviceCategory);
}
