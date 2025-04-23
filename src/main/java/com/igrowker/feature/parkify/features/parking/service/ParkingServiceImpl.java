package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.InvalidAvailabilityException;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginationInfo;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingSummaryResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingServiceImpl implements ParkingService {

    private static final String AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL
            = "Authenticated owner not found with email: ";
    private final ParkingRepository parkingRepository;
    private final AuthUserRepository authUserRepository;

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
                .orElseThrow(() -> new ParkingNotFoundException("Parking not found"));

        if (request.getAvailableSpots() < 0) {
            throw new InvalidAvailabilityException("Available spots cannot be negative");
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
        return mapParkingToDetailsResponse(parking, owner);
    }

    @Override
    @Transactional
    public ParkingResponse createMyParking(@Valid CreateMyParkingRequest request, String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail
                ));
        final Parking parking = Parking.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .hourlyRate(request.getHourlyRate())
                .workingHours(request.getWorkingHours())
                .ownerId(owner.getId())
                .availableSpots(request.getCapacity())
                .parkingPhone(request.getParkingPhone())
                .parkingImageUrl(request.getParkingImageUrl())
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
                            .parkingPhone(p.getParkingPhone())
                            .parkingImageUrl(p.getParkingImageUrl())
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
    @Transactional
    public ParkingAvailabilityResponse updateMyParkingAvailability(
            String ownerEmail,
            Integer availableSpots
    ) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail
                ));
        final Parking parking = parkingRepository.findByOwnerId(owner.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ParkingNotFoundException(
                        "Parking not found for owner with email: " + ownerEmail
                ));
        if (availableSpots > parking.getCapacity()) {
            throw new InvalidAvailabilityException(
                    String.format("Available spots (%d) cannot exceed capacity (%d)",
                            availableSpots, parking.getCapacity())
            );
        }
        if (availableSpots < 0) {
            throw new InvalidAvailabilityException("Available spots cannot be negative.");
        }
        parking.setAvailableSpots(availableSpots);
        parkingRepository.save(parking);
        return new ParkingAvailabilityResponse(parking.getId(), parking.getAvailableSpots());
    }

    @Override
    @Transactional
    public void deleteMyParking(String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail
                ));
        final Parking parking = parkingRepository.findByOwnerId(owner.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ParkingNotFoundException(
                        "Parking not found for owner with email: " + ownerEmail + " to delete."
                ));

        parkingRepository.deleteById(parking.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingAvailabilityResponse> getParkingsAvailability(
            List<Long> parkingIds
    ) {
        final List<Parking> foundParkings = parkingRepository.findAllById(parkingIds);

        return foundParkings.stream()
                .map(parking -> new ParkingAvailabilityResponse(
                        parking.getId(),
                        ofNullable(parking.getAvailableSpots()).orElse(0)
                ))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public ParkingDetailsResponse getMyParkingDetails(String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail
                ));

        final List<Parking> parkings = parkingRepository.findByOwnerId(owner.getId());

        if (parkings.isEmpty()) {
            throw new ParkingNotFoundException(
                    "Parking not found for owner with email: " + ownerEmail
            );
        }

        // Suponemos que quieres devolver solo uno (el primero)
        final Parking parking = parkings.get(0);

        return mapParkingToDetailsResponse(parking, owner);
    }

    private ParkingResponse mapToFlatParkingResponse(Parking parking, AuthUser owner) {
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
                .ownerId(owner.getId())
                .parkingPhone(parking.getParkingPhone())
                .parkingImageUrl(parking.getParkingImageUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerParkingDetailsResponse getOwnerWithParking(String ownerEmail) {
        AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found"));

        List<Parking> parkings = parkingRepository.findByOwnerId(owner.getId());

        if (parkings.isEmpty()) {
            throw new ParkingNotFoundException("Parking not found for owner");
        }

        // Si tienes varios parkings y solo quieres el primero
        Parking parking = parkings.get(0);  // O puedes elegir otro criterio para seleccionar el parking
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
                .ownerId(owner.getId().toString())
                .parkingPhone(parking.getParkingPhone())
                .parkingImageUrl(parking.getParkingImageUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSummaryResponse> getMyParkingSummaries(String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail
                ));

        final List<Parking> parkings = parkingRepository.findByOwnerId(owner.getId());

        return parkings.stream()
                .map(this::mapParkingToSummaryResponse)
                .collect(Collectors.toList());
    }

    private ParkingSummaryResponse mapParkingToSummaryResponse(Parking parking) {
        return ParkingSummaryResponse.builder()
                .id(parking.getId().toString())
                .name(parking.getName())
                .address(parking.getAddress())
                .location(new LocationDto(parking.getLatitude(), parking.getLongitude()))
                .currentAvailability(ofNullable(parking.getAvailableSpots()).orElse(0))
                .hourlyRate(parking.getHourlyRate())
                .parkingPhone(parking.getParkingPhone())
                .parkingImageUrl(parking.getParkingImageUrl())
                .build();
    }

    @Override
    @Transactional
    public ParkingAvailabilityResponse updateSpecificParkingAvailability(
            String ownerEmail, Long parkingId, Integer availableSpots) {

        log.info("Attempting to update availability for parking ID {} by owner {}", parkingId, ownerEmail);

        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated owner not found in DB: {}", ownerEmail);
                    return new OwnerNotFoundException(AUTHENTICATED_OWNER_NOT_FOUND_WITH_EMAIL + ownerEmail);
                });

        final Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> {
                    log.warn("Parking not found with ID: {}", parkingId);
                    return new ParkingNotFoundException("Parking not found with id: " + parkingId);
                });

        if (!parking.getOwnerId().equals(owner.getId())) {
            log.warn("Access denied: Owner {} attempted to modify parking {} owned by {}",
                    ownerEmail, parkingId, parking.getOwnerId());
            throw new AccessDeniedException(String.format(
                    "User %s is not authorized to modify parking with id %d", ownerEmail, parkingId
            ));
        }

        if (availableSpots == null) {
            log.warn("Update failed for parking {}: availableSpots is null", parkingId);
            throw new InvalidAvailabilityException("Available spots cannot be null.");
        }
        if (availableSpots < 0) {
            log.warn("Update failed for parking {}: availableSpots {} is negative", parkingId, availableSpots);
            throw new InvalidAvailabilityException("Available spots cannot be negative.");
        }
        if (availableSpots > parking.getCapacity()) {
            log.warn("Update failed for parking {}: availableSpots {} exceeds capacity {}",
                    parkingId, availableSpots, parking.getCapacity());
            throw new InvalidAvailabilityException(
                    String.format("Available spots (%d) cannot exceed capacity (%d)",
                            availableSpots, parking.getCapacity())
            );
        }

        log.info("Updating parking {} availability from {} to {}",
                parkingId, parking.getAvailableSpots(), availableSpots);
        parking.setAvailableSpots(availableSpots);
        parkingRepository.save(parking);

        log.info("Successfully updated availability for parking {}", parkingId);
        return new ParkingAvailabilityResponse(parking.getId(), parking.getAvailableSpots());
    }

    private record ParkingWithDistance(Parking parking, double distance) {
    }

}
