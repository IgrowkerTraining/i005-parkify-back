package com.igrowker.feature.parkify.features.user.dto.response;

import java.time.LocalDateTime;

// Minimal reservation data to expose in the user detail response
public class ReservationDTO {

    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ReservationDTO(Long id, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }
}
