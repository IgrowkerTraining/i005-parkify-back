package com.igrowker.feature.parkify.features.parking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "parking")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Column(columnDefinition = "TEXT")
    private String description;
    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer capacity;
    @PositiveOrZero
    @Column(name = "available_spots")
    private Integer availableSpots;
    @NotNull
    @PositiveOrZero
    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;
    @Column(name = "working_hours")
    private String workingHours;
    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "parking_phone")
    private String parkingPhone;
    @Column(name = "parking_image_url")
    private String parkingImageUrl;

}