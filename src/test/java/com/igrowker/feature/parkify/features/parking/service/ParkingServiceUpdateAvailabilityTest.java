package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - updateMyParkingAvailability Unit Tests")
class ParkingServiceUpdateAvailabilityTest {

    private static final String OWNER_EMAIL_EXISTS = "owner.exists@test.com";
    private static final String OWNER_EMAIL_NOT_FOUND = "owner.notfound@test.com";
    private static final Long OWNER_ID = 1L;
    private static final Long PARKING_ID = 10L;
    private static final int PARKING_CAPACITY = 20;
    private static final int INITIAL_AVAILABLE_SPOTS = 10;
    private static final int VALID_NEW_AVAILABLE_SPOTS = 15;
    private static final int SPOTS_EXCEEDING_CAPACITY = 25;

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    @Captor
    private ArgumentCaptor<Parking> parkingCaptor;
    private AuthUser testOwner;
    private Parking testParking;

    @BeforeEach
    void setUp() {
        testOwner = new AuthUser();
        testOwner.setId(OWNER_ID);
        testOwner.setEmail(OWNER_EMAIL_EXISTS);
        testParking = Parking.builder()
                .id(PARKING_ID)
                .ownerId(OWNER_ID)
                .capacity(PARKING_CAPACITY)
                .availableSpots(INITIAL_AVAILABLE_SPOTS)
                .build();
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessTests {

        @Test
        @DisplayName("Should update available spots and return correct response when owner and parking exist")
        void updateMyParkingAvailability_OwnerAndParkingExist_ShouldUpdateAndReturnResponse() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(testParking));
            when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            final ParkingAvailabilityResponse response = parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_EXISTS, VALID_NEW_AVAILABLE_SPOTS
            );

            verify(parkingRepository, times(1)).save(parkingCaptor.capture());
            final Parking savedParking = parkingCaptor.getValue();

            assertThat(savedParking.getAvailableSpots())
                    .as("Check saved available spots")
                    .isEqualTo(VALID_NEW_AVAILABLE_SPOTS);
            assertThat(savedParking.getCapacity())
                    .as("Check capacity remains unchanged")
                    .isEqualTo(PARKING_CAPACITY);
            assertThat(response).isNotNull();
            assertThat(response.parkingId())
                    .as("Check response parking ID")
                    .isEqualTo(PARKING_ID);
            assertThat(response.availableSpots())
                    .as("Check response available spots")
                    .isEqualTo(VALID_NEW_AVAILABLE_SPOTS);
            verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_EXISTS);
            verify(parkingRepository, times(1)).findByOwnerId(OWNER_ID);
            verifyNoMoreInteractions(authUserRepository, parkingRepository);
        }

        @Test
        @DisplayName("Should allow updating spots to zero")
        void updateMyParkingAvailability_UpdateToZero_ShouldSucceed() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(testParking));
            when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ParkingAvailabilityResponse response = parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_EXISTS, 0
            );

            verify(parkingRepository).save(parkingCaptor.capture());
            assertThat(parkingCaptor.getValue().getAvailableSpots()).isZero();
            assertThat(response.availableSpots()).isZero();
        }

        @Test
        @DisplayName("Should allow updating spots to full capacity")
        void updateMyParkingAvailability_UpdateToCapacity_ShouldSucceed() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(testParking));
            when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ParkingAvailabilityResponse response = parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_EXISTS, PARKING_CAPACITY
            );

            verify(parkingRepository).save(parkingCaptor.capture());
            assertThat(parkingCaptor.getValue().getAvailableSpots()).isEqualTo(PARKING_CAPACITY);
            assertThat(response.availableSpots()).isEqualTo(PARKING_CAPACITY);
        }
    }

    @Nested
    @DisplayName("Failure Scenarios")
    class FailureTests {

        @Test
        @DisplayName("Should throw OwnerNotFoundException when owner email does not exist")
        void updateMyParkingAvailability_OwnerNotFound_ShouldThrowOwnerNotFoundException() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_NOT_FOUND)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_NOT_FOUND, VALID_NEW_AVAILABLE_SPOTS))
                    .isInstanceOf(OwnerNotFoundException.class)
                    .hasMessageContaining("Authenticated owner not found with email: " + OWNER_EMAIL_NOT_FOUND);

            verify(parkingRepository, never()).findByOwnerId(anyLong());
            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ParkingNotFoundException when owner exists but has no parking")
        void updateMyParkingAvailability_ParkingNotFound_ShouldThrowParkingNotFoundException() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_EXISTS, VALID_NEW_AVAILABLE_SPOTS))
                    .isInstanceOf(ParkingNotFoundException.class)
                    .hasMessageContaining("Parking not found for owner with email: " + OWNER_EMAIL_EXISTS);

            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when available spots exceed capacity")
        void updateMyParkingAvailability_SpotsExceedCapacity_ShouldThrowIllegalArgumentException() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(testParking));

            assertThatThrownBy(() -> parkingService.updateMyParkingAvailability(
                    OWNER_EMAIL_EXISTS, SPOTS_EXCEEDING_CAPACITY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Available spots (%d) cannot exceed capacity (%d)",
                            SPOTS_EXCEEDING_CAPACITY, PARKING_CAPACITY);

            verify(parkingRepository, never()).save(any());
        }

    }
}