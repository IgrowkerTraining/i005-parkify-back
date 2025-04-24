package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.request.UpdateMyParkingRequest;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - updateMyParking (with ID) Unit Tests")
class ParkingServiceUpdateMyParkingWithIdTest {

    private static final String OWNER_EMAIL_EXISTS = "owner.update@test.com";
    private static final String OWNER_EMAIL_NOT_FOUND = "owner.update.notfound@test.com";
    private static final Long OWNER_ID = 1L;
    private static final Long PARKING_ID = 10L;
    private static final Long NON_EXISTENT_PARKING_ID = 99L;
    private static final int INITIAL_CAPACITY = 20;
    private static final int INITIAL_AVAILABLE_SPOTS = 10;

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;

    @Captor
    private ArgumentCaptor<Parking> parkingCaptor;

    private AuthUser testOwner;
    private Parking existingParking;
    private UpdateMyParkingRequest validUpdateRequest;
    private UpdateMyParkingRequest invalidCapacityRequest;
    private UpdateMyParkingRequest sameCapacityRequest;

    @BeforeEach
    void setUp() {
        testOwner = new AuthUser();
        testOwner.setId(OWNER_ID);
        testOwner.setEmail(OWNER_EMAIL_EXISTS);
        testOwner.setUsername("Test Update Owner");
        testOwner.setContactPhone("123456789");

        existingParking = Parking.builder()
                .id(PARKING_ID)
                .ownerId(OWNER_ID)
                .name("Old Name")
                .address("Old Address")
                .latitude(10.0)
                .longitude(10.0)
                .description("Old Description")
                .capacity(INITIAL_CAPACITY)
                .availableSpots(INITIAL_AVAILABLE_SPOTS)
                .hourlyRate(5.0)
                .workingHours("Old Hours")
                .parkingPhone("Old Phone")
                .parkingImageUrl("Old URL")
                .build();

        validUpdateRequest = new UpdateMyParkingRequest(
                "New Name",
                "New Address",
                20.0,
                20.0,
                "New Description",
                25,
                6.0,
                "New Hours",
                "New Phone",
                "New URL"
        );

        invalidCapacityRequest = new UpdateMyParkingRequest(
                "Invalid Cap Name", "Invalid Cap Address", 30.0, 30.0,
                "Desc", 5, 7.0, "Hours", null, null
        );

        sameCapacityRequest = new UpdateMyParkingRequest(
                "Same Cap Name", "Same Cap Address", 40.0, 40.0,
                "Desc", INITIAL_CAPACITY, 8.0, "Hours", null, null
        );
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessTests {

        @Test
        @DisplayName("Should update parking successfully when owner, parking exist, owner owns it, and request is valid")
        void shouldUpdateParkingSuccessfully_whenAllConditionsMet() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(PARKING_ID)).thenReturn(Optional.of(existingParking));
            when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ParkingResponse response = parkingService.updateMyParking(OWNER_EMAIL_EXISTS, PARKING_ID, validUpdateRequest);

            verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_EXISTS);
            verify(parkingRepository, times(1)).findById(PARKING_ID);
            verify(parkingRepository, times(1)).save(parkingCaptor.capture());
            verifyNoMoreInteractions(authUserRepository, parkingRepository);

            Parking capturedParking = parkingCaptor.getValue();

            assertAll("Captured Entity Fields Update",
                    () -> assertThat(capturedParking.getId()).isEqualTo(PARKING_ID),
                    () -> assertThat(capturedParking.getOwnerId()).isEqualTo(OWNER_ID),
                    () -> assertThat(capturedParking.getName()).isEqualTo(validUpdateRequest.name()),
                    () -> assertThat(capturedParking.getAddress()).isEqualTo(validUpdateRequest.address()),
                    () -> assertThat(capturedParking.getLatitude()).isEqualTo(validUpdateRequest.latitude()),
                    () -> assertThat(capturedParking.getLongitude()).isEqualTo(validUpdateRequest.longitude()),
                    () -> assertThat(capturedParking.getDescription()).isEqualTo(validUpdateRequest.description()),
                    () -> assertThat(capturedParking.getCapacity()).isEqualTo(validUpdateRequest.capacity()),
                    () -> assertThat(capturedParking.getHourlyRate()).isEqualTo(validUpdateRequest.hourlyRate()),
                    () -> assertThat(capturedParking.getWorkingHours()).isEqualTo(validUpdateRequest.workingHours()),
                    () -> assertThat(capturedParking.getParkingPhone()).isEqualTo(validUpdateRequest.parkingPhone()),
                    () -> assertThat(capturedParking.getParkingImageUrl()).isEqualTo(validUpdateRequest.parkingImageUrl()),
                    () -> assertThat(capturedParking.getAvailableSpots()).isEqualTo(INITIAL_AVAILABLE_SPOTS) // Не меняется
            );

            assertAll("Response DTO Fields",
                    () -> assertThat(response).isNotNull(),
                    () -> assertThat(response.getId()).isEqualTo(PARKING_ID),
                    () -> assertThat(response.getName()).isEqualTo(validUpdateRequest.name()),
                    () -> assertThat(response.getCurrentAvailability()).isEqualTo(INITIAL_AVAILABLE_SPOTS)
            );
        }

        @Test
        @DisplayName("Should update successfully and skip capacity validation when capacity is not changed")
        void shouldSkipCapacityCheck_whenCapacityIsNotChanged() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(PARKING_ID)).thenReturn(Optional.of(existingParking));
            when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ParkingResponse response = parkingService.updateMyParking(OWNER_EMAIL_EXISTS, PARKING_ID, sameCapacityRequest);

            verify(parkingRepository).save(parkingCaptor.capture());
            Parking captured = parkingCaptor.getValue();

            assertThat(captured.getCapacity()).isEqualTo(INITIAL_CAPACITY);
            assertThat(captured.getName()).isEqualTo(sameCapacityRequest.name());
            assertThat(response.getCapacity()).isEqualTo(INITIAL_CAPACITY);
            assertThat(response.getName()).isEqualTo(sameCapacityRequest.name());
        }
    }

    @Nested
    @DisplayName("Failure Scenarios")
    class FailureTests {

        @Test
        @DisplayName("Should throw OwnerNotFoundException when owner email does not exist")
        void shouldThrowOwnerNotFoundException_whenOwnerEmailDoesNotExist() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_NOT_FOUND)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> parkingService
                    .updateMyParking(OWNER_EMAIL_NOT_FOUND, PARKING_ID, validUpdateRequest))
                    .isInstanceOf(OwnerNotFoundException.class)
                    .hasMessageContaining(OWNER_EMAIL_NOT_FOUND);
            verify(parkingRepository, never()).findById(anyLong());
            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ParkingNotFoundException when parking ID does not exist")
        void shouldThrowParkingNotFoundException_whenParkingIdDoesNotExist() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(NON_EXISTENT_PARKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> parkingService
                    .updateMyParking(OWNER_EMAIL_EXISTS, NON_EXISTENT_PARKING_ID, validUpdateRequest))
                    .isInstanceOf(ParkingNotFoundException.class)
                    .hasMessageContaining(String.valueOf(NON_EXISTENT_PARKING_ID));

            verify(parkingRepository, times(1)).findById(NON_EXISTENT_PARKING_ID);
            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when owner tries to update parking they don't own")
        void shouldThrowAccessDeniedException_whenUpdatingNotOwnedParking() {
            final long anotherOwnerId = OWNER_ID + 1;
            existingParking.setOwnerId(anotherOwnerId);

            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(PARKING_ID)).thenReturn(Optional.of(existingParking));

            assertThatThrownBy(() -> parkingService
                    .updateMyParking(OWNER_EMAIL_EXISTS, PARKING_ID, validUpdateRequest))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining(String.format(
                            "User %s is not authorized to modify parking with id %d", OWNER_EMAIL_EXISTS, PARKING_ID
                    ));

            verify(parkingRepository, times(1)).findById(PARKING_ID);
            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when requested capacity is lower than current available spots")
        void shouldThrowIllegalArgumentException_whenCapacityIsLowerThanAvailableSpots() {
            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(PARKING_ID)).thenReturn(Optional.of(existingParking));

            assertThatThrownBy(() -> parkingService
                    .updateMyParking(OWNER_EMAIL_EXISTS, PARKING_ID, invalidCapacityRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(
                            "Cannot set capacity (%d) lower than current available spots (%d)",
                            invalidCapacityRequest.capacity(), existingParking.getAvailableSpots()
                    ));

            verify(parkingRepository, times(1)).findById(PARKING_ID);
            verify(parkingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when capacity is lower and availableSpots is null")
        void shouldThrowIllegalArgumentException_whenCapacityIsLowerAndAvailableIsNull() {
            existingParking.setAvailableSpots(null);
            existingParking.setCapacity(15);
            UpdateMyParkingRequest lowCapacityRequest = new UpdateMyParkingRequest(
                    "Low Cap", "Low Addr", 1.0, 1.0, "Desc", 10, 1.0, "H", null, null
            );

            when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
            when(parkingRepository.findById(PARKING_ID)).thenReturn(Optional.of(existingParking));

            assertThatThrownBy(() -> parkingService
                    .updateMyParking(OWNER_EMAIL_EXISTS, PARKING_ID, lowCapacityRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(
                            "Cannot set capacity (%d) lower than current available spots (%d)",
                            lowCapacityRequest.capacity(), existingParking.getCapacity()
                    ));

            verify(parkingRepository, never()).save(any());
        }
    }
}