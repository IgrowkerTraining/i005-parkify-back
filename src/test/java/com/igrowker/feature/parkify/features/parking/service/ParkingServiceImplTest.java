package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl Unit Tests")
class ParkingServiceImplTest {

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long INVALID_PARKING_ID = 99L;
    private static final int EXPECTED_AVAILABILITY = 10;
    @Mock
    private ParkingRepository parkingRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;

    @Test
    @DisplayName("getParkingAvailability should return availability when parking exists")
    void getParkingAvailability_ParkingExists_ReturnsAvailability() {
        final Parking mockParking = new Parking();
        mockParking.setId(VALID_PARKING_ID);
        mockParking.setAvailableSpots(EXPECTED_AVAILABILITY);
        when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));

        final ParkingAvailabilityResponse response = parkingService
                .getParkingAvailability(VALID_PARKING_ID);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getParkingId()).isEqualTo(VALID_PARKING_ID),
                () -> assertThat(response.getAvailableSpots()).isEqualTo(EXPECTED_AVAILABILITY)
        );
        verify(parkingRepository, times(1)).findById(VALID_PARKING_ID);
        verifyNoMoreInteractions(parkingRepository);
    }

    @Test
    @DisplayName("getParkingAvailability should return 0 when availableSpots is null")
    void getParkingAvailability_AvailableSpotsNull_ReturnsZeroAvailability() {
        Parking mockParking = new Parking();
        mockParking.setId(VALID_PARKING_ID);
        mockParking.setAvailableSpots(null);
        when(parkingRepository.findById(VALID_PARKING_ID)).thenReturn(Optional.of(mockParking));

        final ParkingAvailabilityResponse response = parkingService
                .getParkingAvailability(VALID_PARKING_ID);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getParkingId()).isEqualTo(VALID_PARKING_ID),
                () -> assertThat(response.getAvailableSpots()).isZero()
        );
        verify(parkingRepository, times(1)).findById(VALID_PARKING_ID);
        verifyNoMoreInteractions(parkingRepository);
    }


    @Test
    @DisplayName("getParkingAvailability should throw ParkingNotFoundException when parking does not exist")
    void getParkingAvailability_ParkingNotFound_ThrowsParkingNotFoundException() {
        when(parkingRepository.findById(INVALID_PARKING_ID)).thenReturn(Optional.empty());

        final ParkingNotFoundException exception = assertThrows(
                ParkingNotFoundException.class,
                () -> parkingService.getParkingAvailability(INVALID_PARKING_ID),
                "Expected ParkingNotFoundException to be thrown"
        );

        assertThat(exception.getMessage()).isEqualTo("Parking not found with id: " + INVALID_PARKING_ID);
        verify(parkingRepository, times(1)).findById(INVALID_PARKING_ID);
        verifyNoMoreInteractions(parkingRepository);
    }
}