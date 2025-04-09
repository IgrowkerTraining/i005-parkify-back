package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl Unit Tests")
class ParkingServiceImplTest {

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long VALID_OWNER_ID = 10L;
    private static final Long INVALID_PARKING_ID = 99L;
    private static final Long OWNER_ID_FOR_MISSING_OWNER = 11L;
    private static final String OWNER_PHONE = "123-456-789";
    private static final int EXPECTED_AVAILABILITY = 10;
    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    private Parking mockParking;
    private AuthUser mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new AuthUser();
        mockOwner.setId(VALID_OWNER_ID);
        mockOwner.setContactPhone(OWNER_PHONE);
        mockParking = Parking.builder()
                .id(VALID_PARKING_ID)
                .name("Test Parking")
                .address("123 Test St")
                .latitude(10.0)
                .longitude(20.0)
                .description("A nice test parking")
                .capacity(50)
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

            assertThat(exception.getMessage()).isEqualTo("Parking not found with id: " + INVALID_PARKING_ID);
            verify(parkingRepository).findById(INVALID_PARKING_ID);
            verify(authUserRepository, never()).findById(any());
            verifyNoMoreInteractions(parkingRepository);
        }

        @Test
        @DisplayName("should throw OwnerNotFoundException when owner does not exist for parking")
        void getParkingDetails_OwnerNotFound_ThrowsOwnerNotFoundException() {
            mockParking.setOwnerId(OWNER_ID_FOR_MISSING_OWNER);
            when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(OWNER_ID_FOR_MISSING_OWNER)).thenReturn(Optional.empty());

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
            verifyNoInteractions(authUserRepository); // Убедимся, что репозиторий юзеров не трогали
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

            assertThat(exception.getMessage()).isEqualTo("Parking not found with id: " + INVALID_PARKING_ID);
            verify(parkingRepository).findById(INVALID_PARKING_ID);
            verifyNoMoreInteractions(parkingRepository);
            verifyNoInteractions(authUserRepository);
        }
    }

    // TODO: add createParking and updateAvailability tests
}