package com.igrowker.miniproject.repositories;

import com.igrowker.miniproject.models.Parking;
import com.igrowker.miniproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IParkingRepository extends JpaRepository<Parking, Long> {
}
