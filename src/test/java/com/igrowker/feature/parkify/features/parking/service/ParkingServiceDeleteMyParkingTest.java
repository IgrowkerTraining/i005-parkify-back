package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - deleteMyParking Unit Tests")
class ParkingServiceDeleteMyParkingTest {

    private static final String OWNER_EMAIL_EXISTS = "owner.delete@test.com";
    private static final String OWNER_EMAIL_NOT_FOUND = "owner.delete.notfound@test.com";
    private static final Long OWNER_ID = 2L;
    private static final Long PARKING_ID = 20L;

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
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
                .build();
    }

    @Test
    @DisplayName("Should call deleteById when owner and parking exist")
    void deleteMyParking_OwnerAndParkingExist_ShouldCallDeleteById() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
        when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(testParking));

        parkingService.deleteMyParking(OWNER_EMAIL_EXISTS);

        verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_EXISTS);
        verify(parkingRepository, times(1)).findByOwnerId(OWNER_ID);
        verify(parkingRepository, times(1)).deleteById(PARKING_ID);
        verify(parkingRepository, never()).delete(any(Parking.class));
        verify(parkingRepository, never()).deleteAll(anyList());
        verifyNoMoreInteractions(authUserRepository, parkingRepository);
    }

    @Test
    @DisplayName("Should throw OwnerNotFoundException when owner email does not exist")
    void deleteMyParking_OwnerNotFound_ShouldThrowOwnerNotFoundException() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_NOT_FOUND)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.deleteMyParking(OWNER_EMAIL_NOT_FOUND))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessageContaining("Authenticated owner not found with email: " + OWNER_EMAIL_NOT_FOUND);
        verify(parkingRepository, never()).findByOwnerId(anyLong());
        verify(parkingRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw ParkingNotFoundException when owner exists but has no parking")
    void deleteMyParking_ParkingNotFound_ShouldThrowParkingNotFoundException() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
        when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> parkingService.deleteMyParking(OWNER_EMAIL_EXISTS))
                .isInstanceOf(ParkingNotFoundException.class)
                .hasMessageContaining("Parking not found for owner with email: " + OWNER_EMAIL_EXISTS);
        verify(parkingRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw ParkingNotFoundException when findByOwnerId returns null")
    void deleteMyParking_FindByOwnerIdReturnsNull_ShouldThrowParkingNotFoundException() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
        when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(null);

        assertThatThrownBy(() -> parkingService.deleteMyParking(OWNER_EMAIL_EXISTS))
                .isInstanceOf(NullPointerException.class);

        verify(parkingRepository, never()).deleteById(anyLong());
    }
}