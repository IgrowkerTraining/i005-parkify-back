package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - getParkingsAvailability Unit Tests")
class ParkingServiceGetParkingsAvailabilityTest {

    @Mock
    private ParkingRepository parkingRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;
    private Parking parking1;
    private Parking parking2;
    private Parking parking3NullSpots;

    @BeforeEach
    void setUp() {
        parking1 = Parking.builder().id(1L).availableSpots(10).build();
        parking2 = Parking.builder().id(2L).availableSpots(5).build();
        parking3NullSpots = Parking.builder().id(3L).availableSpots(null).build();
    }

    @Test
    @DisplayName("Should return availability for all requested existing IDs")
    void getParkingsAvailability_AllIdsExist_ShouldReturnAll() {
        final List<Long> requestedIds = List.of(1L, 2L);
        final List<Parking> foundParkings = List.of(parking1, parking2);
        when(parkingRepository.findAllById(requestedIds)).thenReturn(foundParkings);

        final List<ParkingAvailabilityResponse> result = parkingService
                .getParkingsAvailability(requestedIds);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new ParkingAvailabilityResponse(1L, 10),
                        new ParkingAvailabilityResponse(2L, 5)
                );

        verify(parkingRepository, times(1)).findAllById(requestedIds);
        verifyNoMoreInteractions(parkingRepository);
    }

    @Test
    @DisplayName("Should return availability only for found IDs when some IDs do not exist")
    void getParkingsAvailability_SomeIdsNotExist_ShouldReturnOnlyFound() {
        final List<Long> requestedIds = List.of(1L, 99L, 2L);
        final List<Parking> foundParkings = List.of(parking1, parking2);
        when(parkingRepository.findAllById(requestedIds)).thenReturn(foundParkings);

        final List<ParkingAvailabilityResponse> result = parkingService.getParkingsAvailability(requestedIds);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new ParkingAvailabilityResponse(1L, 10),
                        new ParkingAvailabilityResponse(2L, 5)
                );

        verify(parkingRepository, times(1)).findAllById(requestedIds);
        verifyNoMoreInteractions(parkingRepository);
    }

    @Test
    @DisplayName("Should return empty list when no requested IDs exist")
    void getParkingsAvailability_NoIdsExist_ShouldReturnEmptyList() {
        final List<Long> requestedIds = List.of(98L, 99L);
        when(parkingRepository.findAllById(requestedIds)).thenReturn(Collections.emptyList());

        final List<ParkingAvailabilityResponse> result = parkingService.getParkingsAvailability(requestedIds);

        assertThat(result)
                .isNotNull()
                .isEmpty();
        verify(parkingRepository, times(1)).findAllById(requestedIds);
        verifyNoMoreInteractions(parkingRepository);
    }

    @Test
    @DisplayName("Should return 0 availability when availableSpots is null in the entity")
    void getParkingsAvailability_NullAvailableSpots_ShouldReturnZero() {
        final List<Long> requestedIds = java.util.List.of(1L, 3L);
        final List<Parking> foundParkings = List.of(parking1, parking3NullSpots);
        when(parkingRepository.findAllById(requestedIds)).thenReturn(foundParkings);

        final List<ParkingAvailabilityResponse> result = parkingService.getParkingsAvailability(requestedIds);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new ParkingAvailabilityResponse(1L, 10),
                        new ParkingAvailabilityResponse(3L, 0)
                );
        verify(parkingRepository, times(1)).findAllById(requestedIds);
        verifyNoMoreInteractions(parkingRepository);
    }

    @Test
    @DisplayName("Should return empty list when requested ID list is empty (handled by validation before service)")
    void getParkingsAvailability_EmptyIdList_ShouldReturnEmptyList() {
        final List<Long> requestedIds = Collections.emptyList();
        when(parkingRepository.findAllById(requestedIds)).thenReturn(Collections.emptyList());

        final List<ParkingAvailabilityResponse> result = parkingService.getParkingsAvailability(requestedIds);

        assertThat(result)
                .isNotNull()
                .isEmpty();
        verify(parkingRepository, times(1)).findAllById(requestedIds);
        verifyNoMoreInteractions(parkingRepository);
    }
}