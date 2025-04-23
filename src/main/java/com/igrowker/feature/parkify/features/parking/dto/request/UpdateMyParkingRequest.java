package com.igrowker.feature.parkify.features.parking.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for updating an owner's parking details using PUT.
 * Represents the complete set of editable fields for a parking.
 * availableSpots is updated via a separate PATCH endpoint.
 *
 * @param name            Parking name (required, max 255 chars).
 * @param address         Parking address (required, max 500 chars).
 * @param latitude        Latitude (required, -90 to 90).
 * @param longitude       Longitude (required, -180 to 180).
 * @param description     Optional description (max 1000 chars).
 * @param capacity        Total parking capacity (required, non-negative). Validation against available spots happens in service layer.
 * @param hourlyRate      Hourly rate (required, non-negative).
 * @param workingHours    Optional working hours description (max 255 chars).
 * @param parkingPhone    Optional contact phone for the parking (max 20 chars).
 * @param parkingImageUrl Optional URL for the parking image (max 2048 chars).
 */
@Builder
public record UpdateMyParkingRequest(

        @NotBlank(message = "Parking name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        @NotBlank(message = "Parking address cannot be blank")
        @Size(max = 500, message = "Address cannot exceed 500 characters")
        String address,

        @NotNull(message = "Latitude cannot be null")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double latitude,

        @NotNull(message = "Longitude cannot be null")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double longitude,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Capacity cannot be null")
        @PositiveOrZero(message = "Capacity must be zero or positive")
        Integer capacity,

        @NotNull(message = "Hourly rate cannot be null")
        @PositiveOrZero(message = "Hourly rate must be zero or positive")
        Double hourlyRate,

        @Size(max = 255, message = "Working hours cannot exceed 255 characters")
        String workingHours,

        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        String parkingPhone,

        @Size(max = 2048, message = "Image URL cannot exceed 2048 characters")
        String parkingImageUrl
) {
}