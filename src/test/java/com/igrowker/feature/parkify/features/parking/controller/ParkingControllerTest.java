package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingController Unit Tests")
class ParkingControllerTest {

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long INVALID_PARKING_ID = 99L;
    private static final int EXPECTED_AVAILABILITY = 5;
    @Mock
    private ParkingService parkingService;
    @InjectMocks
    private ParkingController parkingController;


    @Test
    @DisplayName("getParkingAvailability should return OK with availability data " +
            "when parking exists"
    )
    void getParkingAvailability_ParkingExists_ReturnsOkWithData() {
        final ParkingAvailabilityResponse expectedResponse = new ParkingAvailabilityResponse(
                VALID_PARKING_ID, EXPECTED_AVAILABILITY
        );
        when(parkingService.getParkingAvailability(VALID_PARKING_ID)).thenReturn(expectedResponse);

        final ResponseEntity<ParkingAvailabilityResponse> actualResponseEntity = parkingController
                .getParkingAvailability(VALID_PARKING_ID);

        assertAll(
                () -> assertThat(actualResponseEntity).isNotNull(),
                () -> assertThat(actualResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(actualResponseEntity.getBody()).isNotNull(),
                () -> assertThat(actualResponseEntity.getBody().parkingId())
                        .isEqualTo(VALID_PARKING_ID),
                () -> assertThat(actualResponseEntity.getBody().availableSpots())
                        .isEqualTo(EXPECTED_AVAILABILITY)
        );
        verify(parkingService, times(1))
                .getParkingAvailability(VALID_PARKING_ID);
        verifyNoMoreInteractions(parkingService);
    }

    @Test
    @DisplayName("getParkingAvailability should propagate ParkingNotFoundException" +
            " when service throws it"
    )
    void getParkingAvailability_ServiceThrowsNotFound_PropagatesException() {
        final String exceptionMessage = "Parking not found test";
        when(parkingService.getParkingAvailability(INVALID_PARKING_ID))
                .thenThrow(new ParkingNotFoundException(exceptionMessage));

        final ParkingNotFoundException exception = assertThrows(
                ParkingNotFoundException.class,
                () -> parkingController.getParkingAvailability(INVALID_PARKING_ID)
        );

        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
        verify(parkingService, times(1))
                .getParkingAvailability(INVALID_PARKING_ID);
        verifyNoMoreInteractions(parkingService);
    }
}