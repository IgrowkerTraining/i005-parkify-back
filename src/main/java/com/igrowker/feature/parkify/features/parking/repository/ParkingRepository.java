package com.igrowker.feature.parkify.features.parking.repository;

import com.igrowker.feature.parkify.features.parking.entities.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findByOwnerId(Long ownerId);
}
