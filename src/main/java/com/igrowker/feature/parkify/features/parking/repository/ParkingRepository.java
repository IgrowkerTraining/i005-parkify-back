package com.igrowker.feature.parkify.features.parking.repository;

import com.igrowker.feature.parkify.features.parking.entities.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    Optional<Parking> findByOwnerId(Long ownerId);
}
