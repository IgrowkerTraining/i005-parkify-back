package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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

    @Test
    @DisplayName("getOwnerWithParking should return OK with owner and parking data")
    void getOwnerWithParking_ReturnsOkWithData() {
        final String ownerEmail = "owner@example.com";
        final OwnerParkingDetailsResponse expectedResponse = OwnerParkingDetailsResponse.builder()
                .ownerName("Carlos Test")
                .ownerEmail(ownerEmail)
                .ownerPhone("+56999887766")
                .parking(ParkingResponse.builder()
                        .id(1L)
                        .name("Central Parking")
                        .address("123 Always Live St.")
                        .latitude(-33.45)
                        .longitude(-70.66)
                        .description("Spacious and secure")
                        .capacity(50)
                        .currentAvailability(20)
                        .hourlyRate(1000.0)
                        .workingHours("08:00 AM - 08:00 PM")
                        .ownerId(10L)
                        .build())
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(ownerEmail);

        when(parkingService.getOwnerWithParking(ownerEmail)).thenReturn(expectedResponse);

        final ResponseEntity<OwnerParkingDetailsResponse> response =
                parkingController.getOwnerWithParking(authentication); // Método correcto

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().getOwnerEmail()).isEqualTo(ownerEmail),
                () -> assertThat(response.getBody().getParking().getName()).isEqualTo("Central Parking")
        );

        verify(parkingService, times(1)).getOwnerWithParking(ownerEmail);
        verifyNoMoreInteractions(parkingService);
    }

}