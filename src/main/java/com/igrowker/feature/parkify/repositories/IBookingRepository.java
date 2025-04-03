package com.igrowker.feature.parkify.repositories;

import com.igrowker.feature.parkify.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBookingRepository extends JpaRepository<Booking, Long> {
}
