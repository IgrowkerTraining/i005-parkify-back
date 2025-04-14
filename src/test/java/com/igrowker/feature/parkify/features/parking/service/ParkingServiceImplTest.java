package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.FeatureNotFoundException;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import com.igrowker.feature.parkify.features.parking_feature.entity.Feature;
import com.igrowker.feature.parkify.features.parking_feature.repository.FeatureRepository;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendationResponse;
import com.igrowker.feature.parkify.features.recommendation.entities.OccupancyHistory;
import com.igrowker.feature.parkify.features.recommendation.repository.OccupancyHistoryRepository;
import com.igrowker.feature.parkify.features.recommendation.service.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl Unit Tests")
class ParkingServiceImplTest {

    private static final Long VALID_PARKING_ID_LONG = 1L;
    private static final String VALID_PARKING_ID_STR = "1";
    private static final Long VALID_OWNER_ID_LONG = 10L;
    private static final String VALID_OWNER_ID_STR = "10";
    private static final String VALID_OWNER_EMAIL = "owner@test.com";
    private static final String UNKNOWN_OWNER_EMAIL = "unknown@test.com";
    private static final Long INVALID_PARKING_ID_LONG = 99L;
    private static final Long OWNER_ID_FOR_MISSING_OWNER_LONG = 11L;
    private static final int EXPECTED_AVAILABILITY = 10;
    private static final int DEFAULT_CAPACITY = 50;
    private static final String FEATURE_SLUG_COVERED = "covered";
    private static final String FEATURE_SLUG_SECURITY = "security";
    private static final String FEATURE_SLUG_EV = "ev-charging";
    private static final Feature FEATURE_COVERED = Feature.builder()
            .id(1L)
            .slug(FEATURE_SLUG_COVERED)
            .name("Covered")
            .build();
    private static final Feature FEATURE_SECURITY = Feature.builder()
            .id(2L)
            .slug(FEATURE_SLUG_SECURITY)
            .name("Security")
            .build();
    private static final Feature FEATURE_EV = Feature.builder()
            .id(3L)
            .slug(FEATURE_SLUG_EV)
            .name("EV Charging")
            .build();

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private OccupancyHistoryRepository occupancyHistoryRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private FeatureRepository featureRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    @InjectMocks
    private RecommendationServiceImpl recommendationService;
    @Captor
    private ArgumentCaptor<Parking> parkingCaptor;

    private Parking mockParking;
    private AuthUser mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new AuthUser();
        mockOwner.setId(VALID_OWNER_ID_LONG);
        mockOwner.setEmail(VALID_OWNER_EMAIL);
        mockOwner.setContactPhone("123-456-7890");

        mockParking = Parking.builder()
                .id(VALID_PARKING_ID_LONG)
                .name("Test Parking")
                .address("123 Test St")
                .latitude(10.0)
                .longitude(20.0)
                .description("A nice test parking")
                .capacity(DEFAULT_CAPACITY)
                .availableSpots(25)
                .hourlyRate(5.5)
                .workingHours("Mon-Fri 9-18")
                .features(Set.of(FEATURE_COVERED, FEATURE_SECURITY, FEATURE_EV))
                .ownerId(VALID_OWNER_ID_LONG)
                .build();
    }

    @Test
    void getOwnerWithParking_ShouldReturnOwnerAndParkingDetails() {
        String email = "owner@example.com";
        AuthUser owner = new AuthUser();
        owner.setId(1L);
        owner.setUsername("Owner Name");
        owner.setEmail(email);
        owner.setContactPhone("+56999887766");

        Parking parking = new Parking();
        parking.setId(1L);
        parking.setName("Test Parking");
        parking.setAddress("123 Test Address");
        parking.setLatitude(40.7128);
        parking.setLongitude(74.0060);
        parking.setDescription("Test Parking Description");
        parking.setAvailableSpots(5);
        parking.setHourlyRate(10.0);
        parking.setWorkingHours("08:00 AM - 08:00 PM");
        parking.setOwnerId(1L);

        when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(owner));
        when(parkingRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(parking));

        OwnerParkingDetailsResponse response = parkingService.getOwnerWithParking(email);

        assertNotNull(response);
        assertEquals(owner.getUsername(), response.getOwnerName());
        assertEquals(owner.getEmail(), response.getOwnerEmail());
        assertEquals(owner.getContactPhone(), response.getOwnerPhone());

        ParkingResponse parkingResponse = response.getParking();
        assertNotNull(parkingResponse);
        assertEquals(parking.getId(), parkingResponse.getId());
        assertEquals(parking.getName(), parkingResponse.getName());
        assertEquals(parking.getAddress(), parkingResponse.getAddress());
        assertEquals(parking.getLatitude(), parkingResponse.getLatitude());
        assertEquals(parking.getLongitude(), parkingResponse.getLongitude());
        assertEquals(parking.getDescription(), parkingResponse.getDescription());
        assertEquals(parking.getAvailableSpots(), parkingResponse.getCurrentAvailability());
        assertEquals(parking.getHourlyRate(), parkingResponse.getHourlyRate());
        assertEquals(parking.getWorkingHours(), parkingResponse.getWorkingHours());
        assertEquals(parking.getOwnerId(), parkingResponse.getOwnerId());

        verify(authUserRepository, times(1)).findByEmail(email);
        verify(parkingRepository, times(1)).findByOwnerId(owner.getId());
    }

    @Test
    void getOwnerWithParking_ShouldThrowException_WhenOwnerNotFound() {
        String email = "nonexistent@example.com";

        when(authUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> parkingService.getOwnerWithParking(email));

        verify(authUserRepository, times(1)).findByEmail(email);
        verify(parkingRepository, times(0)).findByOwnerId(any());
    }

    @Test
    void getOwnerWithParking_ShouldThrowException_WhenParkingNotFound() {
        String email = "owner@example.com";
        AuthUser owner = new AuthUser();
        owner.setId(1L);
        owner.setUsername("Owner Name");
        owner.setEmail(email);
        owner.setContactPhone("+56999887766");

        when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(owner));
        when(parkingRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> parkingService.getOwnerWithParking(email));

        verify(authUserRepository, times(1)).findByEmail(email);
        verify(parkingRepository, times(1)).findByOwnerId(owner.getId());
    }

    @Test
    void generateRecommendations_shouldReturnHighAvailabilityParkings() {
        Parking parking1 = Parking.builder()
                .id(1L)
                .name("Parking A")
                .address("Address A")
                .latitude(40.0)
                .longitude(-3.0)
                .description("Description A")
                .capacity(100)
                .availableSpots(80)
                .hourlyRate(5.0)
                .workingHours("8AM-10PM")
                .ownerId(1L)
                .features(new HashSet<>())
                .build();

        Parking parking2 = Parking.builder()
                .id(2L)
                .name("Parking B")
                .address("Address B")
                .latitude(41.0)
                .longitude(-3.5)
                .description("Description B")
                .capacity(50)
                .availableSpots(20)
                .hourlyRate(4.0)
                .workingHours("24/7")
                .ownerId(2L)
                .features(new HashSet<>())
                .build();

        List<Parking> allParkings = List.of(parking1, parking2);

        OccupancyHistory occupancyHistory1 = OccupancyHistory.builder()
                .id(1L)
                .parkingId(1L)
                .timestamp(LocalDateTime.now().minusDays(1))
                .occupancyRate(0.2)
                .build();

        OccupancyHistory occupancyHistory2 = OccupancyHistory.builder()
                .id(2L)
                .parkingId(2L)
                .timestamp(LocalDateTime.now().minusDays(1))
                .occupancyRate(0.8)
                .build();

        List<OccupancyHistory> history = List.of(occupancyHistory1, occupancyHistory2);

        when(parkingRepository.findAll()).thenReturn(allParkings);
        when(occupancyHistoryRepository.findRecentHistory(any(LocalDateTime.class))).thenReturn(history);

        List<RecommendationResponse> result = recommendationService.generateRecommendations();

        assertEquals(1, result.size());
        assertEquals("Parking A", result.get(0).getName());
    }

    @Nested
    @DisplayName("Get Parking Details (#23)")
    class GetParkingDetailsTests {

        @Test
        @DisplayName("should return correct ParkingDetailsResponse when parking and owner exist")
        void getParkingDetails_ParkingAndOwnerExist_ReturnsCorrectDto() {
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner));

            final ParkingDetailsResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID_LONG);

            assertThat(actualResponse).isNotNull();
            assertAll("ParkingDetailsResponse validation",
                    () -> assertThat(actualResponse.getId())
                            .isEqualTo(VALID_PARKING_ID_STR),
                    () -> assertThat(actualResponse.getName())
                            .isEqualTo(mockParking.getName()),
                    () -> assertThat(actualResponse.getAddress())
                            .isEqualTo(mockParking.getAddress()),
                    () -> assertThat(actualResponse.getLocation()).isNotNull(),
                    () -> assertThat(actualResponse.getLocation().latitude())
                            .isEqualTo(mockParking.getLatitude()),
                    () -> assertThat(actualResponse.getLocation().longitude())
                            .isEqualTo(mockParking.getLongitude()),
                    () -> assertThat(actualResponse.getDescription())
                            .isEqualTo(mockParking.getDescription()),
                    () -> assertThat(actualResponse.getCapacity())
                            .isEqualTo(mockParking.getCapacity()),
                    () -> assertThat(actualResponse.getCurrentAvailability())
                            .isEqualTo(mockParking.getAvailableSpots()),
                    () -> assertThat(actualResponse.getHourlyRate())
                            .isEqualTo(mockParking.getHourlyRate()),
                    () -> assertThat(actualResponse.getWorkingHours())
                            .isEqualTo(mockParking.getWorkingHours()),
                    () -> assertThat(actualResponse.getFeatureSlugs())
                            .containsExactlyInAnyOrder(
                                    FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY, FEATURE_SLUG_EV
                            ),
                    () -> assertThat(actualResponse.getOwnerId())
                            .isEqualTo(VALID_OWNER_ID_STR)
            );
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository).findById(VALID_OWNER_ID_LONG);
            verifyNoMoreInteractions(parkingRepository, authUserRepository);
        }

        @Test
        @DisplayName("should return zero availability when availableSpots is null in entity")
        void getParkingDetails_NullAvailableSpots_ReturnsZeroAvailability() {
            mockParking.setAvailableSpots(null);
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner));

            final ParkingDetailsResponse actualResponse = parkingService
                    .getParkingDetails(VALID_PARKING_ID_LONG);

            assertThat(actualResponse.getCurrentAvailability()).isZero();
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository).findById(VALID_OWNER_ID_LONG);
        }

        @Test
        @DisplayName("should return empty feature list when features Set is empty or null in entity")
        void getParkingDetails_EmptyOrNullFeatures_ReturnsEmptyList() {
            mockParking.setFeatures(Collections.emptySet()); // Используем пустой Set
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner));

            ParkingDetailsResponse actualResponseEmpty = parkingService
                    .getParkingDetails(VALID_PARKING_ID_LONG);
            assertThat(actualResponseEmpty.getFeatureSlugs()).isNotNull().isEmpty();

            mockParking.setFeatures(null);
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner)); // Не забываем мок владельца

            ParkingDetailsResponse actualResponseNull = parkingService
                    .getParkingDetails(VALID_PARKING_ID_LONG);
            assertThat(actualResponseNull.getFeatureSlugs()).isNotNull().isEmpty();

            verify(parkingRepository, times(2)).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository, times(2)).findById(VALID_OWNER_ID_LONG);
        }

        @Test
        @DisplayName("should throw ParkingNotFoundException when parking does not exist")
        void getParkingDetails_ParkingNotFound_ThrowsParkingNotFoundException() {
            when(parkingRepository.findById(INVALID_PARKING_ID_LONG)).thenReturn(Optional.empty());

            final ParkingNotFoundException exception = assertThrows(ParkingNotFoundException.class, () ->
                    parkingService.getParkingDetails(INVALID_PARKING_ID_LONG)
            );

            assertThat(exception.getMessage())
                    .isEqualTo("Parking not found with id: " + INVALID_PARKING_ID_LONG);
            verify(parkingRepository).findById(INVALID_PARKING_ID_LONG);
            verifyNoInteractions(authUserRepository);
        }

        @Test
        @DisplayName("should throw OwnerNotFoundException when owner does not exist for parking")
        void getParkingDetails_OwnerNotFound_ThrowsOwnerNotFoundException() {
            mockParking.setOwnerId(OWNER_ID_FOR_MISSING_OWNER_LONG);
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(OWNER_ID_FOR_MISSING_OWNER_LONG))
                    .thenReturn(Optional.empty());

            final OwnerNotFoundException exception = assertThrows(OwnerNotFoundException.class, () ->
                    parkingService.getParkingDetails(VALID_PARKING_ID_LONG)
            );

            assertThat(exception.getMessage()).isEqualTo("Owner not found with id: "
                    + OWNER_ID_FOR_MISSING_OWNER_LONG);
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository).findById(OWNER_ID_FOR_MISSING_OWNER_LONG);
            verifyNoMoreInteractions(parkingRepository, authUserRepository);
        }
    }

    @Nested
    @DisplayName("Get Parking Availability (#25)")
    class GetParkingAvailabilityTests {
        @BeforeEach
        void availabilitySetUp() {
            mockParking.setAvailableSpots(EXPECTED_AVAILABILITY);
        }

        @Test
        @DisplayName("should return availability when parking exists")
        void getParkingAvailability_ParkingExists_ReturnsAvailability() {
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));

            final ParkingAvailabilityResponse response = parkingService
                    .getParkingAvailability(VALID_PARKING_ID_LONG);

            assertThat(response).isNotNull();
            assertAll("Availability response validation",
                    () -> assertThat(response.parkingId()).isEqualTo(VALID_PARKING_ID_LONG),
                    () -> assertThat(response.availableSpots()).isEqualTo(EXPECTED_AVAILABILITY)
            );
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }

        @Test
        @DisplayName("should return 0 when availableSpots is null in entity")
        void getParkingAvailability_AvailableSpotsNull_ReturnsZeroAvailability() {
            mockParking.setAvailableSpots(null);
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));

            final ParkingAvailabilityResponse response = parkingService
                    .getParkingAvailability(VALID_PARKING_ID_LONG);

            assertThat(response).isNotNull();
            assertAll("Null availability response validation",
                    () -> assertThat(response.parkingId()).isEqualTo(VALID_PARKING_ID_LONG),
                    () -> assertThat(response.availableSpots()).isZero()
            );
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }


        @Test
        @DisplayName("should throw ParkingNotFoundException when parking does not exist")
        void getParkingAvailability_ParkingNotFound_ThrowsParkingNotFoundException() {
            when(parkingRepository.findById(INVALID_PARKING_ID_LONG)).thenReturn(Optional.empty());

            final ParkingNotFoundException exception = assertThrows(
                    ParkingNotFoundException.class,
                    () -> parkingService.getParkingAvailability(INVALID_PARKING_ID_LONG),
                    "Expected ParkingNotFoundException to be thrown"
            );

            assertThat(exception.getMessage())
                    .isEqualTo("Parking not found with id: " + INVALID_PARKING_ID_LONG);
            verify(parkingRepository).findById(INVALID_PARKING_ID_LONG);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }
    }

    @Nested
    @DisplayName("Create My Parking (#15 - Step 2)")
    class CreateMyParkingTests {

        private CreateMyParkingRequest validRequest;
        private CreateMyParkingRequest requestWithoutFeatures;
        private CreateMyParkingRequest requestWithUnknownFeature;
        private Parking savedParkingEntity;
        private Long newParkingId = 2L;

        @BeforeEach
        void createMyParkingSetup() {
            validRequest = CreateMyParkingRequest.builder()
                    .name("My New Parking")
                    .address("15 New St")
                    .latitude(40.0)
                    .longitude(-3.0)
                    .description("Brand new parking")
                    .capacity(DEFAULT_CAPACITY)
                    .hourlyRate(6.0)
                    .workingHours("Mon-Sun 00-24")
                    .featureSlugs(List.of(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY))
                    .build();

            requestWithoutFeatures = CreateMyParkingRequest.builder()
                    .name("No Feature Parking")
                    .address("16 Plain St")
                    .latitude(41.0)
                    .longitude(-4.0)
                    .capacity(10)
                    .hourlyRate(2.0)
                    .build();

            requestWithUnknownFeature = CreateMyParkingRequest.builder()
                    .name("Unknown Feature Parking")
                    .address("17 Error St")
                    .latitude(42.0)
                    .longitude(-5.0)
                    .capacity(5)
                    .hourlyRate(1.0)
                    .featureSlugs(List.of(FEATURE_SLUG_COVERED, "unknown-feature"))
                    .build();

            savedParkingEntity = Parking.builder()
                    .id(newParkingId)
                    .name(validRequest.getName())
                    .address(validRequest.getAddress())
                    .latitude(validRequest.getLatitude())
                    .longitude(validRequest.getLongitude())
                    .description(validRequest.getDescription())
                    .capacity(validRequest.getCapacity())
                    .hourlyRate(validRequest.getHourlyRate())
                    .workingHours(validRequest.getWorkingHours())
                    .features(Set.of(FEATURE_COVERED, FEATURE_SECURITY))
                    .ownerId(VALID_OWNER_ID_LONG)
                    .availableSpots(validRequest.getCapacity())
                    .build();
        }

        @Test
        @DisplayName("should create parking with features and return ParkingResponse when owner exists and features found")
        void createMyParking_OwnerAndFeaturesExist_CreatesAndReturnsParkingResponse() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(featureRepository.findBySlugIn(Set.of(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY)))
                    .thenReturn(Set.of(FEATURE_COVERED, FEATURE_SECURITY));
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            verify(featureRepository).findBySlugIn(Set.of(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY));
            verify(parkingRepository).save(parkingCaptor.capture());

            final Parking capturedParking = parkingCaptor.getValue();
            assertAll("Captured Parking Entity validation",
                    () -> assertThat(capturedParking.getName()).isEqualTo(validRequest.getName()),
                    () -> assertThat(capturedParking.getCapacity()).isEqualTo(validRequest.getCapacity()),
                    () -> assertThat(capturedParking.getAvailableSpots()).isEqualTo(validRequest.getCapacity()),
                    () -> assertThat(capturedParking.getOwnerId()).isEqualTo(VALID_OWNER_ID_LONG),
                    () -> assertThat(capturedParking.getFeatures())
                            .containsExactlyInAnyOrder(FEATURE_COVERED, FEATURE_SECURITY)
            );

            assertAll("ParkingResponse validation after creation",
                    () -> assertNotNull(actualResponse),
                    () -> assertEquals(newParkingId, actualResponse.getId()),
                    () -> assertEquals(savedParkingEntity.getName(), actualResponse.getName()),
                    () -> assertEquals(savedParkingEntity.getCapacity(), actualResponse.getCapacity()),
                    () -> assertEquals(savedParkingEntity.getAvailableSpots(), actualResponse.getCurrentAvailability()),
                    () -> assertEquals(savedParkingEntity.getOwnerId(), actualResponse.getOwnerId()),
                    () -> assertThat(actualResponse.getFeatureSlugs())
                            .containsExactlyInAnyOrder(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY)
            );

            verifyNoMoreInteractions(authUserRepository, parkingRepository, featureRepository);
        }

        @Test
        @DisplayName("should create parking without features when request has empty featureSlugs list")
        void createMyParking_NoFeaturesRequested_CreatesParkingWithEmptyFeatures() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            savedParkingEntity.setName(requestWithoutFeatures.getName());
            savedParkingEntity.setFeatures(Collections.emptySet());
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(requestWithoutFeatures, VALID_OWNER_EMAIL);

            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            verify(featureRepository, never()).findBySlugIn(any());
            verify(parkingRepository).save(parkingCaptor.capture());

            final Parking capturedParking = parkingCaptor.getValue();
            assertThat(capturedParking.getFeatures()).isNotNull().isEmpty();

            assertThat(actualResponse.getFeatureSlugs()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("should throw FeatureNotFoundException when a requested feature slug does not exist")
        void createMyParking_UnknownFeatureSlug_ThrowsFeatureNotFoundException() {
            final String unknownSlug = "unknown-feature";
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(featureRepository.findBySlugIn(Set.of(FEATURE_SLUG_COVERED, unknownSlug)))
                    .thenReturn(Set.of(FEATURE_COVERED));

            final FeatureNotFoundException exception = assertThrows(
                    FeatureNotFoundException.class,
                    () -> parkingService.createMyParking(requestWithUnknownFeature, VALID_OWNER_EMAIL)
            );

            assertThat(exception.getMessage()).isEqualTo("Feature not found with slug: " + unknownSlug);
            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            verify(featureRepository).findBySlugIn(Set.of(FEATURE_SLUG_COVERED, unknownSlug));
            verify(parkingRepository, never()).save(any(Parking.class));
        }


        @Test
        @DisplayName("should throw OwnerNotFoundException when owner email does not exist")
        void createMyParking_OwnerNotFound_ThrowsOwnerNotFoundException() {
            when(authUserRepository.findByEmail(UNKNOWN_OWNER_EMAIL)).thenReturn(Optional.empty());

            final OwnerNotFoundException exception = assertThrows(
                    OwnerNotFoundException.class,
                    () -> parkingService.createMyParking(validRequest, UNKNOWN_OWNER_EMAIL)
            );

            assertThat(exception.getMessage()).isEqualTo("Authenticated owner not found with email: " + UNKNOWN_OWNER_EMAIL);
            verify(authUserRepository).findByEmail(UNKNOWN_OWNER_EMAIL);
            verifyNoInteractions(featureRepository, parkingRepository);
        }

        @Test
        @DisplayName("should propagate DataAccessException when repository save fails")
        void createMyParking_RepositorySaveFails_ThrowsDataAccessException() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(featureRepository.findBySlugIn(Set.of(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY)))
                    .thenReturn(Set.of(FEATURE_COVERED, FEATURE_SECURITY));
            final DataAccessException dbException = new DataAccessException(
                    "Simulated database connection error"
            ) {
            };
            when(parkingRepository.save(any(Parking.class))).thenThrow(dbException);

            final DataAccessException thrown = assertThrows(DataAccessException.class, () ->
                    parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL)
            );

            assertEquals(dbException, thrown);
            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            verify(featureRepository).findBySlugIn(Set.of(FEATURE_SLUG_COVERED, FEATURE_SLUG_SECURITY));
            verify(parkingRepository).save(any(Parking.class));
        }

        @Test
        @DisplayName("should correctly initialize availableSpots with capacity on creation")
        void createMyParking_ShouldInitializeAvailabilityWithCapacity() {
            final int specificCapacity = 77;
            validRequest.setCapacity(specificCapacity);
            validRequest.setFeatureSlugs(Collections.emptyList());
            savedParkingEntity.setCapacity(specificCapacity);
            savedParkingEntity.setAvailableSpots(specificCapacity);
            savedParkingEntity.setFeatures(Collections.emptySet());

            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(parkingRepository).save(parkingCaptor.capture());
            final Parking capturedParking = parkingCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedParking.getAvailableSpots())
                            .as("Captured entity: Available spots should be initialized with capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCurrentAvailability())
                            .as("Response DTO: Should reflect initial availability equal to capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCapacity()).isEqualTo(specificCapacity)
            );
        }

    }

    @Nested
    @DisplayName("Update Parking Availability (#27)")
    class UpdateAvailabilityTests {

    }
}