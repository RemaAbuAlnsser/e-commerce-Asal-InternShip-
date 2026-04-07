package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.DeliveryCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryCityRepository extends JpaRepository<DeliveryCity, Long> {

    boolean existsByCityNameIgnoreCase(String cityName);

    Optional<DeliveryCity> findByCityNameIgnoreCase(String cityName);

    boolean existsByCityNameIgnoreCaseAndIdNot(String cityName, Long id);
}