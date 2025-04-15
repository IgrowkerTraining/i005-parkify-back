package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.UpdateAvailabilityRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.*;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "")
@RestController
@RequestMapping("/api/v1/parkings")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

    //#18
    @GetMapping("/owner/parking")
    public ResponseEntity<OwnerParkingDetailsResponse> getOwnerWithParking(Authentication authentication) {
        String ownerEmail = authentication.getName();
        OwnerParkingDetailsResponse response = parkingService.getOwnerWithParking(ownerEmail);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //21
    @GetMapping("/nearby")
    public ResponseEntity<PaginatedParkingResponse> getNearbyParkings(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minAvailability,
            @RequestParam(required = false) List<String> features,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        PaginatedParkingResponse response = parkingService.findNearbyParkings(
                lat, lon, radius, maxPrice, minAvailability, features, limit, offset, pageable
        );
        return ResponseEntity.ok(response);
    }


    // #20, #22
    @GetMapping
    public ResponseEntity<PaginatedParkingResponse> findParkings(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "1000") Integer radius,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minAvailability,
            @RequestParam(name = "features", required = false) List<String> featureSlugs,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            Pageable pageable
    ) {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                latitude, longitude, radius, maxPrice, minAvailability, featureSlugs,
                limit, offset, pageable);
        return ResponseEntity.ok(response);
    }

    // #25
    @GetMapping("/{parkingId}/availability")
    public ResponseEntity<ParkingAvailabilityResponse> getParkingAvailability
    (@PathVariable Long parkingId
    ) {
        ParkingAvailabilityResponse response = parkingService.getParkingAvailability(parkingId);
        return ResponseEntity.ok(response);
    }

    @Deprecated(since = "2025.04.10")
    @PostMapping("/create")
    public ResponseEntity<ParkingResponse> createParking(@RequestBody ParkingRequest request) {
        ParkingResponse response = parkingService.createParking(request);
        return ResponseEntity.ok(response);
    }

    @Deprecated(since = "2025.04.10")
    @PutMapping("/update-availability")
    public ResponseEntity<ParkingResponse> updateAvailability(
            @RequestBody ParkingRequest request) {
        return ResponseEntity.ok(parkingService.updateAvailability(request));
    }

    @PutMapping("/{parkingId}/features/{featureSlug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFeatureToParking(
            @PathVariable Long parkingId,
            @PathVariable String featureSlug,
            Authentication authentication
    ) {
        final String ownerEmail = authentication.getName();
        parkingService.associateFeature(ownerEmail, parkingId, featureSlug);
    }

    @DeleteMapping("/{parkingId}/features/{featureSlug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFeatureFromParking(
            @PathVariable Long parkingId,
            @PathVariable String featureSlug,
            Authentication authentication
    ) {
        String ownerEmail = authentication.getName();
        parkingService.disassociateFeature(ownerEmail, parkingId, featureSlug);
    }

    // #23
    @GetMapping("/{parkingId}")
    public ResponseEntity<ParkingDetailsResponse> getParkingDetails(@PathVariable Long parkingId) {
        final ParkingDetailsResponse response = parkingService.getParkingDetails(parkingId);
        return ResponseEntity.ok(response);
    }

    // #15 (step 2)
    @PostMapping("/my")
    public ResponseEntity<ParkingResponse> createMyParking(
            @Valid @RequestBody CreateMyParkingRequest request,
            Authentication authentication
    ) {
        final String ownerEmail = authentication.getName();
        final ParkingResponse createdParking = parkingService.createMyParking(request, ownerEmail);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/v1/parkings/{id}")
                .buildAndExpand(createdParking.getId()).toUri();
        return ResponseEntity.created(location).body(createdParking);
    }

    // #26
    @GetMapping("/my")
    public ResponseEntity<ParkingDetailsResponse> getMyParking(Authentication authentication) {
        final String ownerEmail = authentication.getName();
        final ParkingDetailsResponse response = parkingService.getMyParkingDetails(ownerEmail);
        return ResponseEntity.ok(response);
    }

    // #27
    @PatchMapping("/my/availability")
    public ResponseEntity<ParkingAvailabilityResponse> updateMyParkingAvailability(
            @Valid @RequestBody UpdateAvailabilityRequest request,
            Authentication authentication) {
        final String ownerEmail = authentication.getName();
        final ParkingAvailabilityResponse updatedAvailability = parkingService
                .updateMyParkingAvailability(ownerEmail, request.availableSpots());
        return ResponseEntity.ok(updatedAvailability);
    }

}
