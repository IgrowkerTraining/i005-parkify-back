package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.FeatureNotFoundException;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.*;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import com.igrowker.feature.parkify.features.parking_feature.entity.Feature;
import com.igrowker.feature.parkify.features.parking_feature.repository.FeatureRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingRepository parkingRepository;
    private final AuthUserRepository authUserRepository;
    private final FeatureRepository featureRepository;

    @Override
    public ParkingResponse createParking(ParkingRequest request) {
        final Parking parking = Parking.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .hourlyRate(request.getRateHour())
                .ownerId(request.getOwnerId())
                .build();
        final Parking saved = parkingRepository.save(parking);
        return ParkingResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .hourlyRate(saved.getHourlyRate())
                .ownerId(saved.getOwnerId())
                .build();
    }

    @Override
    public ParkingResponse updateAvailability(ParkingRequest request) {
        Parking parking = parkingRepository.findById(request.getParkingId())
                .orElseThrow(() -> new RuntimeException("Parking not found"));

        if (request.getAvailableSpots() < 0) {
            throw new IllegalArgumentException("Available spots cannot be negative");
        }

        parking.setAvailableSpots(request.getAvailableSpots());
        parkingRepository.save(parking);

        return ParkingResponse.builder()
                .id(parking.getId())
                .currentAvailability(parking.getAvailableSpots())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public ParkingAvailabilityResponse getParkingAvailability(Long parkingId) {
        final Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ParkingNotFoundException(
                        "Parking not found with id: " + parkingId
                ));
        final int availability = ofNullable(parking.getAvailableSpots())
                .orElse(0);
        return new ParkingAvailabilityResponse(parking.getId(), availability);
    }

    @Transactional(readOnly = true)
    @Override
    public ParkingDetailsResponse getParkingDetails(Long parkingId) {
        final Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ParkingNotFoundException("Parking not found with id: " + parkingId));
        final AuthUser owner = authUserRepository.findById(parking.getOwnerId())
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Owner not found with id: " + parking.getOwnerId()
                ));
        final List<String> slugs = Optional.ofNullable(parking.getFeatures())
                .stream()
                .flatMap(Collection::stream)
                .map(Feature::getSlug)
                .toList();
        return ParkingDetailsResponse.builder()
                .id(parking.getId().toString())
                .name(parking.getName())
                .address(parking.getAddress())
                .location(new LocationDto(parking.getLatitude(), parking.getLongitude()))
                .description(parking.getDescription())
                .capacity(parking.getCapacity())
                .currentAvailability(ofNullable(parking.getAvailableSpots()).orElse(0))
                .hourlyRate(parking.getHourlyRate())
                .workingHours(parking.getWorkingHours())
                .featureSlugs(slugs)
                .ownerId(owner.getId().toString())
                .build();
    }

    @Override
    @Transactional
    public ParkingResponse createMyParking(@Valid CreateMyParkingRequest request, String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Authenticated owner not found with email: " + ownerEmail
                ));
        Set<Feature> featuresSet = new HashSet<>();
        final List<String> requestedSlugs = request.getFeatureSlugs();

        if (requestedSlugs != null && !requestedSlugs.isEmpty()) {
            final Set<String> uniqueRequestedSlugs = new HashSet<>(requestedSlugs);
            featuresSet = featureRepository.findBySlugIn(uniqueRequestedSlugs);
            if (featuresSet.size() != uniqueRequestedSlugs.size()) {
                final Set<String> foundSlugs = featuresSet.stream()
                        .map(Feature::getSlug)
                        .collect(Collectors.toSet());
                final String missingSlug = uniqueRequestedSlugs.stream()
                        .filter(slug -> !foundSlugs.contains(slug))
                        .findFirst()
                        .orElse("unknown slug");
                throw new FeatureNotFoundException("Feature not found with slug: " + missingSlug);
            }
        }
        final Parking parking = Parking.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .hourlyRate(request.getHourlyRate())
                .workingHours(request.getWorkingHours())
                .features(featuresSet)
                .ownerId(owner.getId())
                .availableSpots(request.getCapacity())
                .build();
        final Parking savedParking = parkingRepository.save(parking);

        return mapToFlatParkingResponse(savedParking, owner);
    }

    /**
     * Finds nearby parkings based on geographical coordinates and applies optional filters.
     * Note: This implementation performs filtering and sorting in memory after fetching all parkings.
     * For large datasets, performance optimization by moving logic to the database query is recommended.
     *
     * @param latitude        The latitude of the search center.
     * @param longitude       The longitude of the search center.
     * @param radius          Optional maximum distance from the center in *kilometers*. If null, distance is not filtered.
     * @param maxPrice        Optional maximum hourly rate.
     * @param minAvailability Optional minimum number of available spots.
     * @param featureSlugs    Optional list of required feature slugs.
     * @param limit           The maximum number of results per page.
     * @param offset          The starting offset for pagination.
     * @param pageable        (Currently unused in favor of manual limit/offset, but kept for signature compatibility)
     * @return A paginated response containing parking summaries.
     */
    //21
    @Override
    @Transactional(readOnly = true)
    public PaginatedParkingResponse findNearbyParkings(
            Double latitude,
            Double longitude,
            Integer radius,
            Double maxPrice,
            Integer minAvailability,
            List<String> featureSlugs,
            int limit,
            int offset,
            Pageable pageable
    ) {
        List<Parking> allParkings = parkingRepository.findAll();

        List<ParkingWithDistance> filtered = allParkings.stream()
                .map(p -> new ParkingWithDistance(p, haversine(latitude, longitude, p.getLatitude(), p.getLongitude())))
                .filter(pwd -> {
                    final Parking p = pwd.parking;
                    // 1. Filtro por distancia (si radius está seteado)
                    double distance = haversine(latitude, longitude, p.getLatitude(), p.getLongitude());
                    if (radius != null && distance > radius) return false;

                    // 2. Filtro por precio
                    if (maxPrice != null && p.getHourlyRate() != null && p.getHourlyRate() > maxPrice)
                        return false;

                    // 3. Filtro por spots disponibles
                    if (minAvailability != null && ofNullable(p.getAvailableSpots()).orElse(0) < minAvailability)
                        return false;

                    // 4. Filtro por features (si están presentes)
                    if (featureSlugs != null && !featureSlugs.isEmpty()) {
                        // Aquí obtenemos las características del parking
                        Set<String> parkingFeatures = (p.getFeatures() == null)
                                ? Collections.emptySet()
                                : p.getFeatures().stream()
                                        .map(Feature::getSlug)
                                        .collect(Collectors.toSet());

                        // Verificamos si las características del parking contienen todas las solicitadas
                        if (!parkingFeatures.containsAll(featureSlugs)) return false;
                    }

                    return true;
                })
                // Ordenamos por cercanía
                .sorted(Comparator.comparingDouble(ParkingWithDistance::distance))
                .toList();

        // Total sin paginar
        int total = filtered.size();

        // Paginación
        List<ParkingSummaryResponse> paginatedList = filtered.stream()
                .skip(offset)
                .limit(limit)
                .map(pwd -> {
                    Parking p = pwd.parking();
                    int availability = ofNullable(p.getAvailableSpots()).orElse(0);
                    return ParkingSummaryResponse.builder()
                            .id(p.getId().toString())
                            .name(p.getName())
                            .address(p.getAddress())
                            .location(new LocationDto(p.getLatitude(), p.getLongitude()))
                            .hourlyRate(p.getHourlyRate())
                            .currentAvailability(availability)
                            .distance(pwd.distance())
                            .build();
                })
                .toList();

        PaginationInfo paginationInfo = new PaginationInfo(total, limit, offset);

        return new PaginatedParkingResponse(paginatedList, paginationInfo);

    }


    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    @Override
    public ParkingAvailabilityResponse updateMyParkingAvailability(
            String ownerEmail, Integer availableSpots
    ) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingDetailsResponse getMyParkingDetails(String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Authenticated owner not found with email: " + ownerEmail
                ));
        final Parking parking = parkingRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ParkingNotFoundException(
                        "Parking not found for owner with email: " + ownerEmail
                ));
        return mapParkingToDetailsResponse(parking, owner);
    }

    @Override
    public void associateFeature(String ownerEmail, Long parkingId, String featureSlug) {

    }

    @Override
    public void disassociateFeature(String ownerEmail, Long parkingId, String featureSlug) {

    }

    private ParkingResponse mapToFlatParkingResponse(Parking parking, AuthUser owner) {
        final List<String> featureSlugsList = Optional.ofNullable(parking.getFeatures())
                .filter(features -> !features.isEmpty())
                .map(features -> features.stream()
                        .map(Feature::getSlug)
                        .toList())
                .orElse(Collections.emptyList());
        return ParkingResponse.builder()
                .id(parking.getId())
                .name(parking.getName())
                .address(parking.getAddress())
                .latitude(parking.getLatitude())
                .longitude(parking.getLongitude())
                .description(parking.getDescription())
                .capacity(parking.getCapacity())
                .currentAvailability(ofNullable(parking.getAvailableSpots()).orElse(0))
                .hourlyRate(parking.getHourlyRate())
                .workingHours(parking.getWorkingHours())
                .featureSlugs(featureSlugsList)
                .ownerId(owner.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerParkingDetailsResponse getOwnerWithParking(String ownerEmail) {
        AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Parking parking = parkingRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Parking not found for owner"));

        List<String> featureSlugs = Optional.ofNullable(parking.getFeatures())
                .orElse(Collections.emptySet())
                .stream()
                .map(Feature::getSlug)
                .toList();

        ParkingResponse parkingResponse = ParkingResponse.builder()
                .id(parking.getId())
                .name(parking.getName())
                .address(parking.getAddress())
                .latitude(parking.getLatitude())
                .longitude(parking.getLongitude())
                .description(parking.getDescription())
                .capacity(parking.getCapacity())
                .currentAvailability(parking.getAvailableSpots())
                .hourlyRate(parking.getHourlyRate())
                .workingHours(parking.getWorkingHours())
                .featureSlugs(featureSlugs)
                .ownerId(owner.getId())
                .build();

        return OwnerParkingDetailsResponse.builder()
                .ownerName(owner.getUsername())
                .ownerEmail(owner.getEmail())
                .ownerPhone(owner.getContactPhone())
                .parking(parkingResponse)
                .build();
    }


    private ParkingDetailsResponse mapParkingToDetailsResponse(Parking parking, AuthUser owner) {
        final List<String> featureSlugs = Optional.ofNullable(parking.getFeatures())
                .orElse(Collections.emptySet())
                .stream()
                .map(Feature::getSlug)
                .toList();

        return ParkingDetailsResponse.builder()
                .id(parking.getId().toString())
                .name(parking.getName())
                .address(parking.getAddress())
                .location(new LocationDto(parking.getLatitude(), parking.getLongitude()))
                .description(parking.getDescription())
                .capacity(parking.getCapacity())
                .currentAvailability(ofNullable(parking.getAvailableSpots()).orElse(0))
                .hourlyRate(parking.getHourlyRate())
                .workingHours(parking.getWorkingHours())
                .featureSlugs(featureSlugs)
                .ownerId(owner.getId().toString())
                .build();
    }

    private record ParkingWithDistance(Parking parking, double distance) {
    }

}
