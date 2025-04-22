package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingSummaryResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - findNearbyParkings Unit Tests (No Features)") 
class ParkingServiceImplFindNearbyTest {

    @Mock
    private ParkingRepository parkingRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    private static final double CENTER_LAT = 40.0;
    private static final double CENTER_LON = -3.0;
    private static final double DISTANCE_OFFSET = 0.01;

    @BeforeEach
    void setUp() {
        final Parking parking1NearCheapFull = Parking.builder() // ~11.12 km
                .id(1L).name("Near Cheap Full").latitude(40.1).longitude(-3.0)
                .hourlyRate(5.0).availableSpots(0).capacity(50)
                .ownerId(10L).build(); 
        final Parking parking2MidMidPriceAvail = Parking.builder() // ~22.24 km
                .id(2L).name("Mid MidPrice Avail").latitude(40.2).longitude(-3.0)
                .hourlyRate(10.0).availableSpots(5).capacity(30)
                .ownerId(11L).build(); 
        final Parking parking3FarExpensiveAvail = Parking.builder() // ~55.60 km
                .id(3L).name("Far Expensive Avail").latitude(40.5).longitude(-3.0)
                .hourlyRate(15.0).availableSpots(10).capacity(100)
                .ownerId(12L).build(); 
        final Parking parking4NearMidPriceNullAvail = Parking.builder() // ~7.00 km
                .id(4L).name("Near MidPrice NullAvail").latitude(40.05).longitude(-3.05)
                .hourlyRate(10.0).availableSpots(null).capacity(20)
                .ownerId(13L).build(); 
        final Parking parking5NearNullPriceAvail = Parking.builder() // ~9.86 km
                .id(5L).name("Near NullPrice Avail").latitude(40.08).longitude(-2.95)
                .hourlyRate(null).availableSpots(15).capacity(40)
                .ownerId(14L).build(); 
        final Parking parking6NearMidPriceAvail = Parking.builder() // ~7.87 km
                .id(6L).name("Near MidPrice Avail").latitude(40.0).longitude(-2.9)
                .hourlyRate(9.0).availableSpots(8).capacity(10)
                .ownerId(15L).build(); 

        when(parkingRepository.findAll()).thenReturn(List.of(
                parking1NearCheapFull,
                parking2MidMidPriceAvail,
                parking3FarExpensiveAvail,
                parking4NearMidPriceNullAvail,
                parking5NearNullPriceAvail,
                parking6NearMidPriceAvail
        ));
    }

    @Test
    @DisplayName("Should return all parkings sorted by distance when no filters applied")
    void findNearbyParkings_NoFilters_ReturnsAllSortedByDistance() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                20, 0, Pageable.unpaged()
        );

        assertAll("No filter response",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.pagination().total()).isEqualTo(6),
                () -> assertThat(response.pagination().limit()).isEqualTo(20),
                () -> assertThat(response.pagination().offset()).isZero(),
                () -> assertThat(response.data()).hasSize(6),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("4", "6", "5", "1", "2", "3"),
                () -> assertThat(response.data().get(0).getDistance())
                        .isCloseTo(7.00, offset(DISTANCE_OFFSET)),
                () -> assertThat(response.data().get(5).getDistance())
                        .isCloseTo(55.60, offset(DISTANCE_OFFSET))
        );
    }

    @Test
    @DisplayName("Should filter by radius")
    void findNearbyParkings_FilterByRadius_ReturnsOnlyWithinRadius() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 15, null, null,
                20, 0, Pageable.unpaged()
        );

        assertAll("Radius filter response",
                () -> assertThat(response.pagination().total()).isEqualTo(4),
                () -> assertThat(response.data()).hasSize(4),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("4", "6", "5", "1")
        );
    }

    @Test
    @DisplayName("Should filter by maxPrice, including null price")
    void findNearbyParkings_FilterByMaxPrice_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, 10.0, null,
                20, 0, Pageable.unpaged()
        );

        assertAll("MaxPrice filter response",
                () -> assertThat(response.pagination().total()).isEqualTo(5),
                () -> assertThat(response.data()).hasSize(5),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("4", "6", "5", "1", "2")
        );
    }

    @Test
    @DisplayName("Should filter by minAvailability, treating null as 0")
    void findNearbyParkings_FilterByMinAvailability_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, 5,
                20, 0, Pageable.unpaged()
        );

        assertAll("MinAvailability filter response",
                () -> assertThat(response.pagination().total()).isEqualTo(4),
                () -> assertThat(response.data()).hasSize(4),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("6", "5", "2", "3")
        );
    }

    @Test
    @DisplayName("Should apply combined filters correctly (radius, availability)")
    void findNearbyParkings_CombinedFilters_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 30, null, 5,
                20, 0, Pageable.unpaged()
        );

        assertAll("Combined Filters response (radius, availability)",
                () -> assertThat(response.pagination().total()).isEqualTo(3),
                () -> assertThat(response.data()).hasSize(3),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("6", "5", "2")
        );
    }

    @Test
    @DisplayName("Should apply pagination correctly")
    void findNearbyParkings_Pagination_ReturnsCorrectSlice() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                2, 2, Pageable.unpaged()
        );

        assertAll("Pagination response",
                () -> assertThat(response.pagination().total()).isEqualTo(6),
                () -> assertThat(response.pagination().limit()).isEqualTo(2),
                () -> assertThat(response.pagination().offset()).isEqualTo(2),
                () -> assertThat(response.data()).hasSize(2),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("5", "1")
        );
    }

    @Test
    @DisplayName("Should return only parking with null price when maxPrice is very low")
    void findNearbyParkings_VeryLowMaxPrice_ReturnsOnlyNullPriceParking() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 100, 1.0, null,
                20, 0, Pageable.unpaged()
        );

        assertAll("Low max price matches only null price",
                () -> assertThat(response.pagination().total()).isEqualTo(1),
                () -> assertThat(response.data()).hasSize(1),
                () -> assertThat(response.data().get(0).getId()).isEqualTo("5")
        );
    }

    @Test
    @DisplayName("Should return empty result when no parkings match filters (e.g., very small radius)")
    void findNearbyParkings_NoMatchesDueToRadius_ReturnsEmptyResult() {
        PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 1, null, null,
                20, 0, Pageable.unpaged()); // radius = 1 km

        assertAll("No matches response",
                () -> assertThat(response.pagination().total()).isZero(),
                () -> assertThat(response.data()).isNotNull().isEmpty()
        );
    }
}