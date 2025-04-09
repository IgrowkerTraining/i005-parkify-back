package com.igrowker.feature.parkify.features.parking.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="parkings")
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rateHour;
    private int available;
    private String whatsapp;

    @Column(name = "owner_id")
    private Long ownerId;
    @Column(name = "available_spots")
    private Integer availableSpots;
}
