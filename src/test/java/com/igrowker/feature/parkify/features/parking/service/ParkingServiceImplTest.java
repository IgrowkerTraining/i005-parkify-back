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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.any;

import java.util.Collections;
import java.util.List;

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

    @DisplayName("getParkingDetailsWithPagination should return a page of parkings when valid input is given")
    @Test
    void getParkingDetailsWithPagination_ValidInput_ReturnsPagedParkings() {
        int page = 0;
        int size = 2;

        Parking parking1 = new Parking();
        parking1.setId(1L);
        parking1.setName("Parking A");

        Parking parking2 = new Parking();
        parking2.setId(2L);
        parking2.setName("Parking B");

        Page<Parking> parkingPage = new PageImpl<>(List.of(parking1, parking2));
        when(parkingRepository.findAll(any(Pageable.class))).thenReturn(parkingPage);

        Page<Parking> result = parkingService.getParkingDetailsWithPagination(page, size);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Parking A");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Parking B");

        verify(parkingRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(parkingRepository);
    }

    @DisplayName("getParkingDetailsWithPagination should return empty page when no results are found")
    @Test
    void getParkingDetailsWithPagination_EmptyResults_ReturnsEmptyPage() {
        int page = 0;
        int size = 10;

        Page<Parking> emptyPage = new PageImpl<>(Collections.emptyList());
        when(parkingRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<Parking> result = parkingService.getParkingDetailsWithPagination(page, size);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(parkingRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(parkingRepository);
    }
}