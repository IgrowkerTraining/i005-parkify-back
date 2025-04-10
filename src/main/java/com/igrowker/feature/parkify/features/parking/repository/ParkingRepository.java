package com.igrowker.feature.parkify.features.parking.repository;

import com.igrowker.feature.parkify.features.parking.entities.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingRepository extends JpaRepository<Parking, Long> {

    @Query(value = """
        SELECT p.*, (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(p.latitude)) *
                cos(radians(p.longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(p.latitude))
            )
        ) AS distance
        FROM parkings p
        ORDER BY distance
        """,
            countQuery = "SELECT count(*) FROM parkings",
            nativeQuery = true)
    Page<Object[]> findNearbyParkings(@Param("lat") double lat, @Param("lon") double lon, Pageable pageable);
}

