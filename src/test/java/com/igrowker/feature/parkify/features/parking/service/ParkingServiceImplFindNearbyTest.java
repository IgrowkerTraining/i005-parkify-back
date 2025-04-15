package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingSummaryResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import com.igrowker.feature.parkify.features.parking_feature.entity.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - findNearbyParkings Unit Tests")
class ParkingServiceImplFindNearbyTest {

    @Mock
    private ParkingRepository parkingRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    private static final double CENTER_LAT = 40.0;
    private static final double CENTER_LON = -3.0;
    private static final double DISTANCE_OFFSET = 0.01;
    private static final Feature FEAT_COVERED = Feature.builder()
            .id(1L).slug("covered").name("Covered").build();
    private static final Feature FEAT_SECURE = Feature.builder()
            .id(2L).slug("secure").name("Secure").build();
    private static final Feature FEAT_EV = Feature.builder()
            .id(3L).slug("ev").name("EV Charging").build();

    @BeforeEach
    void setUp() {
        final Parking parking1NearCheapFullCovered = Parking.builder() // ~11.12 km
                .id(1L).name("Near Cheap Full Covered").latitude(40.1).longitude(-3.0)
                .hourlyRate(5.0).availableSpots(0).capacity(50)
                .features(Set.of(FEAT_COVERED)).ownerId(10L).build();
        final Parking parking2MidMidPriceAvailSecureEv = Parking.builder() // ~22.24 km
                .id(2L).name("Mid MidPrice Avail SecureEv").latitude(40.2).longitude(-3.0)
                .hourlyRate(10.0).availableSpots(5).capacity(30)
                .features(Set.of(FEAT_SECURE, FEAT_EV)).ownerId(11L).build();
        final Parking parking3FarExpensiveAvailAllFeat = Parking.builder() // ~55.60 km
                .id(3L).name("Far Expensive Avail AllFeat").latitude(40.5).longitude(-3.0)
                .hourlyRate(15.0).availableSpots(10).capacity(100)
                .features(Set.of(FEAT_COVERED, FEAT_SECURE, FEAT_EV)).ownerId(12L).build();
        final Parking parking4NearMidPriceNullAvailNoFeat = Parking.builder() // ~7.00 km
                .id(4L).name("Near MidPrice NullAvail NoFeat").latitude(40.05).longitude(-3.05)
                .hourlyRate(10.0).availableSpots(null).capacity(20)
                .features(Collections.emptySet()).ownerId(13L).build();
        final Parking parking5NearNullPriceAvailCoveredSecure = Parking.builder() // ~9.86 km
                .id(5L).name("Near NullPrice Avail CoveredSecure").latitude(40.08).longitude(-2.95)
                .hourlyRate(null).availableSpots(15).capacity(40)
                .features(Set.of(FEAT_COVERED, FEAT_SECURE)).ownerId(14L).build();
        final Parking parking6NearMidPriceAvailNullFeat = Parking.builder() // ~7.87 km
                .id(6L).name("Near MidPrice Avail NullFeat").latitude(40.0).longitude(-2.9)
                .hourlyRate(9.0).availableSpots(8).capacity(10).features(null).ownerId(15L).build();
        when(parkingRepository.findAll()).thenReturn(List.of(
                parking1NearCheapFullCovered,
                parking2MidMidPriceAvailSecureEv,
                parking3FarExpensiveAvailAllFeat,
                parking4NearMidPriceNullAvailNoFeat,
                parking5NearNullPriceAvailCoveredSecure,
                parking6NearMidPriceAvailNullFeat
        ));
    }

    @Test
    @DisplayName("Criteria 9: Should return all parkings sorted by distance when no filters applied")
    void findNearbyParkings_NoFilters_ReturnsAllSortedByDistance() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                null, 20, 0, Pageable.unpaged()
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
    @DisplayName("Criteria 4: Should filter by radius")
    void findNearbyParkings_FilterByRadius_ReturnsOnlyWithinRadius() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 15, null, null,
                null, 20, 0, Pageable.unpaged()
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
    @DisplayName("Criteria 5: Should filter by maxPrice, including null price")
    void findNearbyParkings_FilterByMaxPrice_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, 10.0, null,
                null, 20, 0, Pageable.unpaged()
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
    @DisplayName("Criteria 6: Should filter by minAvailability, treating null as 0")
    void findNearbyParkings_FilterByMinAvailability_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, 5,
                null, 20, 0, Pageable.unpaged()
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
    @DisplayName("Criteria 7: Should filter by single feature")
    void findNearbyParkings_FilterBySingleFeature_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                List.of("secure"), 20, 0, Pageable.unpaged()
        );

        assertAll("Single Feature filter response",
                () -> assertThat(response.pagination().total()).isEqualTo(3),
                () -> assertThat(response.data()).hasSize(3),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("5", "2", "3")
        );
    }

    @Test
    @DisplayName("Criteria 7: Should filter by single feature when parking features are null")
    void findNearbyParkings_FilterBySingleFeature_ParkingFeaturesNull() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                List.of("secure"), 20, 0, Pageable.unpaged()
        );

        assertThat(response.data())
                .extracting(ParkingSummaryResponse::getId)
                .doesNotContain("6");
        assertThat(response.pagination().total()).isEqualTo(3);
    }

    @Test
    @DisplayName("Criteria 7: Should filter by multiple features (AND logic)")
    void findNearbyParkings_FilterByMultipleFeatures_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                List.of("covered", "secure"), 20, 0, Pageable.unpaged()
        );

        assertAll("Multiple Features filter response",
                () -> assertThat(response.pagination().total()).isEqualTo(2),
                () -> assertThat(response.data()).hasSize(2),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("5", "3")
        );
    }

    @Test
    @DisplayName("Criteria 8: Should apply combined filters correctly")
    void findNearbyParkings_CombinedFilters_ReturnsCorrectParkings() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, 30, null, 5,
                List.of("secure"), 20, 0, Pageable.unpaged()
        );

        assertAll("Combined Filters response",
                () -> assertThat(response.pagination().total()).isEqualTo(2),
                () -> assertThat(response.data()).hasSize(2),
                () -> assertThat(response.data())
                        .extracting(ParkingSummaryResponse::getId)
                        .containsExactly("5", "2")
        );
    }

    @Test
    @DisplayName("Criteria 10: Should apply pagination correctly")
    void findNearbyParkings_Pagination_ReturnsCorrectSlice() {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                CENTER_LAT, CENTER_LON, null, null, null,
                null, 2, 2, Pageable.unpaged()
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
                CENTER_LAT, CENTER_LON, 100, 1.0,
                null, null, 20, 0, Pageable.unpaged()
        );

        assertAll("Low max price matches only null price",
                () -> assertThat(response.pagination().total()).isEqualTo(1),
                () -> assertThat(response.data()).hasSize(1),
                () -> assertThat(response.data().get(0).getId()).isEqualTo("5")
        );
    }
}