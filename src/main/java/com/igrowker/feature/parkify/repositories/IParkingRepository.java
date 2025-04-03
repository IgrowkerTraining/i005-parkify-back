package com.igrowker.feature.parkify.repositories;

import com.igrowker.feature.parkify.models.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IParkingRepository extends JpaRepository<Parking, Long> {
}
