package com.igrowker.miniproject.repositories;

import com.igrowker.miniproject.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBookingRepository extends JpaRepository<Booking, Long> {
}
