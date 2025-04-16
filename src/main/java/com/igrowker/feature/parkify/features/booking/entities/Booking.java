package com.igrowker.feature.parkify.features.booking.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "start_date")
    private Object startDate;

    @Column(name = "end_date")
    public Object getEndDate() {
        return null;
    }

    public void setEndDate(LocalDateTime now) {
    }

    public void setId(UUID uuid) {
    }
}
