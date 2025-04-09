package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
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

import java.util.Optional;

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

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long VALID_OWNER_ID = 10L;
    private static final String VALID_OWNER_EMAIL = "owner@test.com";
    private static final String UNKNOWN_OWNER_EMAIL = "unknown@test.com";
    private static final Long INVALID_PARKING_ID = 99L;
    private static final Long OWNER_ID_FOR_MISSING_OWNER = 11L;
    private static final int EXPECTED_AVAILABILITY = 10;
    private static final int DEFAULT_CAPACITY = 50;

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    @Captor
    private ArgumentCaptor<Parking> parkingCaptor;
    private Parking mockParking;
    private AuthUser mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new AuthUser();
        mockOwner.setId(VALID_OWNER_ID);
        mockOwner.setEmail(VALID_OWNER_EMAIL);
        mockParking = Parking.builder()
                .id(VALID_PARKING_ID)
                .name("Test Parking")
                .address("123 Test St")
                .latitude(10.0)
                .longitude(20.0)
                .description("A nice test parking")
                .capacity(DEFAULT_CAPACITY)
                .availableSpots(25)
                .hourlyRate(5.5)
                .workingHours("Mon-Fri 9-18")
                .features("covered, security , ev_charging , ")
                .ownerId(VALID_OWNER_ID)
                .build();
    }

    @Nested
    @DisplayName("Get Parking Details (#23)")
    class GetParkingDetailsTests {

        @Test
        @DisplayName("should return full details when parking and owner exist")
        void getParkingDetails_ParkingAndOwnerExist_ReturnsCorrectDto() {
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID)).thenReturn(Optional.of(mockOwner));

            final ParkingResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID);

            assertThat(actualResponse).isNotNull();
            assertAll("ParkingResponse detailed validation",
                    () -> assertThat(actualResponse.getId())
                            .isEqualTo(VALID_PARKING_ID),
                    () -> assertThat(actualResponse.getName())
                            .isEqualTo(mockParking.getName()),
                    () -> assertThat(actualResponse.getAddress())
                            .isEqualTo(mockParking.getAddress()),
                    () -> assertThat(actualResponse.getLatitude())
                            .isEqualTo(mockParking.getLatitude()),
                    () -> assertThat(actualResponse.getLongitude())
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
                    () -> assertThat(actualResponse.getFeatures())
                            .containsExactlyInAnyOrder("covered", "security", "ev_charging"),
                    () -> assertThat(actualResponse.getOwnerId())
                            .isEqualTo(VALID_OWNER_ID)
            );
            verify(parkingRepository).findById(VALID_PARKING_ID);
            verify(authUserRepository).findById(VALID_OWNER_ID);
            verifyNoMoreInteractions(parkingRepository, authUserRepository);
        }

        @Test
        @DisplayName("should return zero availability when availableSpots is null")
        void getParkingDetails_NullAvailableSpots_ReturnsZeroAvailability() {
            mockParking.setAvailableSpots(null);
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID)).thenReturn(Optional.of(mockOwner));

            final ParkingResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID);

            assertThat(actualResponse.getCurrentAvailability()).isZero();
        }

        @Test
        @DisplayName("should return empty feature list when features string is empty")
        void getParkingDetails_EmptyFeatures_ReturnsEmptyList() {
            mockParking.setFeatures(""); // Пустая строка
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID)).thenReturn(Optional.of(mockOwner));

            final ParkingResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID);

            assertThat(actualResponse.getFeatures()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("should return empty feature list when features string is null")
        void getParkingDetails_NullFeatures_ReturnsEmptyList() {
            mockParking.setFeatures(null);
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID)).thenReturn(Optional.of(mockOwner));

            final ParkingResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID);

            assertThat(actualResponse.getFeatures()).isNotNull().isEmpty();
        }


        @Test
        @DisplayName("should throw ParkingNotFoundException when parking does not exist")
        void getParkingDetails_ParkingNotFound_ThrowsParkingNotFoundException() {
            when(parkingRepository.findById(INVALID_PARKING_ID)).thenReturn(Optional.empty());

            final ParkingNotFoundException exception = assertThrows(ParkingNotFoundException.class, () ->
                    parkingService.getParkingDetails(INVALID_PARKING_ID)
            );

            assertThat(exception.getMessage())
                    .isEqualTo("Parking not found with id: " + INVALID_PARKING_ID);
            verify(parkingRepository).findById(INVALID_PARKING_ID);
            verify(authUserRepository, never()).findById(any());
            verifyNoMoreInteractions(parkingRepository);
        }

        @Test
        @DisplayName("should throw OwnerNotFoundException when owner does not exist for parking")
        void getParkingDetails_OwnerNotFound_ThrowsOwnerNotFoundException() {
            mockParking.setOwnerId(OWNER_ID_FOR_MISSING_OWNER);
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(OWNER_ID_FOR_MISSING_OWNER))
                    .thenReturn(Optional.empty());

            final OwnerNotFoundException exception = assertThrows(OwnerNotFoundException.class, () ->
                    parkingService.getParkingDetails(VALID_PARKING_ID)
            );

            assertThat(exception.getMessage()).isEqualTo("Owner not found with id: "
                    + OWNER_ID_FOR_MISSING_OWNER
                    + " for parking id: "
                    + VALID_PARKING_ID);
            verify(parkingRepository).findById(VALID_PARKING_ID);
            verify(authUserRepository).findById(OWNER_ID_FOR_MISSING_OWNER);
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
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));

            final ParkingAvailabilityResponse response = parkingService
                    .getParkingAvailability(VALID_PARKING_ID);

            assertThat(response).isNotNull();
            assertAll("Availability response validation",
                    () -> assertThat(response.getParkingId()).isEqualTo(VALID_PARKING_ID),
                    () -> assertThat(response.getAvailableSpots()).isEqualTo(EXPECTED_AVAILABILITY)
            );
            verify(parkingRepository).findById(VALID_PARKING_ID);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }

        @Test
        @DisplayName("should return 0 when availableSpots is null")
        void getParkingAvailability_AvailableSpotsNull_ReturnsZeroAvailability() {
            mockParking.setAvailableSpots(null);
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));

            final ParkingAvailabilityResponse response = parkingService
                    .getParkingAvailability(VALID_PARKING_ID);

            assertThat(response).isNotNull();
            assertAll("Null availability response validation",
                    () -> assertThat(response.getParkingId()).isEqualTo(VALID_PARKING_ID),
                    () -> assertThat(response.getAvailableSpots()).isZero() // Ожидаем 0
            );
            verify(parkingRepository).findById(VALID_PARKING_ID);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }


        @Test
        @DisplayName("should throw ParkingNotFoundException when parking does not exist")
        void getParkingAvailability_ParkingNotFound_ThrowsParkingNotFoundException() {
            when(parkingRepository.findById(INVALID_PARKING_ID)).thenReturn(Optional.empty());

            final ParkingNotFoundException exception = assertThrows(
                    ParkingNotFoundException.class,
                    () -> parkingService.getParkingAvailability(INVALID_PARKING_ID),
                    "Expected ParkingNotFoundException to be thrown"
            );

            assertThat(exception.getMessage())
                    .isEqualTo("Parking not found with id: " + INVALID_PARKING_ID);
            verify(parkingRepository).findById(INVALID_PARKING_ID);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }
    }

    @Nested
    @DisplayName("Create My Parking (#15)")
    class CreateMyParkingTests {

        private CreateMyParkingRequest validRequest;
        private Parking savedParkingEntity;

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
                    .features("self-service, lighting")
                    .build();

            savedParkingEntity = Parking.builder()
                    .id(2L)
                    .name(validRequest.getName())
                    .address(validRequest.getAddress())
                    .latitude(validRequest.getLatitude())
                    .longitude(validRequest.getLongitude())
                    .description(validRequest.getDescription())
                    .capacity(validRequest.getCapacity())
                    .hourlyRate(validRequest.getHourlyRate())
                    .workingHours(validRequest.getWorkingHours())
                    .features(validRequest.getFeatures())
                    .ownerId(VALID_OWNER_ID)
                    .availableSpots(validRequest.getCapacity())
                    .build();
        }

        @Test
        @DisplayName("should create parking and return response when owner exists")
        void createMyParking_OwnerExists_CreatesAndReturnsParkingResponse() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(authUserRepository, times(1)).findByEmail(VALID_OWNER_EMAIL);
            verify(parkingRepository, times(1)).save(parkingCaptor.capture());
            final Parking capturedParking = parkingCaptor.getValue();
            assertAll(
                    () -> assertAll("Captured Parking Entity validation",
                            () -> assertThat(capturedParking.getName())
                                    .isEqualTo(validRequest.getName()),
                            () -> assertThat(capturedParking.getAddress())
                                    .isEqualTo(validRequest.getAddress()),
                            () -> assertThat(capturedParking.getCapacity())
                                    .isEqualTo(validRequest.getCapacity()),
                            () -> assertThat(capturedParking.getAvailableSpots())
                                    .isEqualTo(validRequest.getCapacity()),
                            () -> assertThat(capturedParking.getOwnerId())
                                    .isEqualTo(VALID_OWNER_ID)
                    ),
                    () -> assertAll("ParkingResponse validation after creation",
                            () -> assertNotNull(actualResponse),
                            () -> assertEquals(savedParkingEntity.getId(),
                                    actualResponse.getId()),
                            () -> assertEquals(savedParkingEntity.getName(),
                                    actualResponse.getName()),
                            () -> assertEquals(savedParkingEntity.getCapacity(),
                                    actualResponse.getCapacity()),
                            () -> assertEquals(savedParkingEntity.getAvailableSpots(),
                                    actualResponse.getCurrentAvailability()),
                            () -> assertEquals(savedParkingEntity.getOwnerId(),
                                    actualResponse.getOwnerId()),
                            () -> assertThat(actualResponse.getFeatures())
                                    .containsExactlyInAnyOrder("self-service", "lighting")
                    )
            );

            verifyNoMoreInteractions(authUserRepository, parkingRepository);
        }

        @Test
        @DisplayName("should throw OwnerNotFoundException when owner email does not exist")
        void createMyParking_OwnerNotFound_ThrowsOwnerNotFoundException() {
            when(authUserRepository.findByEmail(UNKNOWN_OWNER_EMAIL)).thenReturn(Optional.empty());

            final OwnerNotFoundException exception = assertThrows(
                    OwnerNotFoundException.class,
                    () -> parkingService.createMyParking(validRequest, UNKNOWN_OWNER_EMAIL)
            );

            assertThat(exception.getMessage()).contains(UNKNOWN_OWNER_EMAIL);
            verify(authUserRepository, times(1))
                    .findByEmail(UNKNOWN_OWNER_EMAIL);
            verify(parkingRepository, never()).save(any(Parking.class));
            verifyNoMoreInteractions(authUserRepository);
        }

        @Test
        @DisplayName("should propagate DataAccessException when repository save fails")
        void createMyParking_RepositorySaveFails_ThrowsDataAccessException() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            final DataAccessException dbException = new DataAccessException(
                    "Database connection error"
            ) {
            };
            when(parkingRepository.save(any(Parking.class))).thenThrow(dbException);

            final DataAccessException thrown = assertThrows(DataAccessException.class, () ->
                    parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL)
            );

            assertEquals(dbException, thrown);
            verify(authUserRepository, times(1)).findByEmail(VALID_OWNER_EMAIL);
            verify(parkingRepository, times(1)).save(any(Parking.class));
            verifyNoMoreInteractions(authUserRepository, parkingRepository);
        }

        @Test
        @DisplayName("should correctly initialize availableSpots with capacity")
        void createMyParking_ShouldInitializeAvailabilityWithCapacity() {
            final int specificCapacity = 77;
            validRequest.setCapacity(specificCapacity);
            savedParkingEntity.setCapacity(specificCapacity);
            savedParkingEntity.setAvailableSpots(specificCapacity);
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(parkingRepository).save(parkingCaptor.capture());
            final Parking capturedParking = parkingCaptor.getValue();
            assertAll(
                    () -> assertThat(capturedParking.getAvailableSpots())
                            .as("Available spots should be initialized with capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCurrentAvailability())
                            .as("Response DTO should reflect initial availability equal to capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCapacity()).isEqualTo(specificCapacity)
            );
        }
    }

    @Nested
    @DisplayName("Update Parking Availability (#27 - Placeholder)")
    class UpdateAvailabilityTests {
    }

    @Nested
    @DisplayName("Old Create Parking (Legacy/Remove?)")
    class OldCreateParkingTests {
    }
}