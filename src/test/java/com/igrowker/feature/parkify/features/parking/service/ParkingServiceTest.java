package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {
    @Mock
    private ParkingRepository parkingRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    void createParking_ShouldSaveAndReturnParkingResponse() {
        ParkingRequest request = ParkingRequest.builder()
                .name("Test Parking")
                .address("123 Calle Falsa")
                .latitude(10.0)
                .longitude(20.0)
                .rateHour(5.0)
                .available(1)
                .whatsapp("123456789")
                .ownerId(100L)
                .build();

        Parking savedParking = new Parking();
        savedParking.setId(1L);
        savedParking.setName(request.getName());
        savedParking.setAddress(request.getAddress());
        savedParking.setLatitude(request.getLatitude());
        savedParking.setLongitude(request.getLongitude());
        savedParking.setRateHour(request.getRateHour());
        savedParking.setAvailable(request.getAvailable());
        savedParking.setWhatsapp(request.getWhatsapp());
        savedParking.setOwnerId(request.getOwnerId());

        Mockito.when(parkingRepository.save(Mockito.any(Parking.class)))
                .thenReturn(savedParking);

        ParkingResponse response = parkingService.createParking(request);

        assertEquals("Test Parking", response.getName());
        assertEquals(1L, response.getId());
    }

    @Test
    void updateAvailability_ShouldUpdateAvailableSpots() {
        Parking parking = new Parking();
        parking.setId(1L);
        parking.setAvailableSpots(2);

        Mockito.when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));

        ParkingRequest request = ParkingRequest.builder()
                .parkingId(1L)
                .availableSpots(5)
                .build();

        ParkingResponse response = parkingService.updateAvailability(request);

        assertEquals(5, response.getAvailableSpots());
        Mockito.verify(parkingRepository).save(parking);
    }

    @Test
    void updateAvailability_ShouldThrowException_WhenParkingNotFound() {
        Mockito.when(parkingRepository.findById(1L)).thenReturn(Optional.empty());

        ParkingRequest request = ParkingRequest.builder()
                .parkingId(1L)
                .availableSpots(3)
                .build();

        assertThrows(RuntimeException.class, () -> parkingService.updateAvailability(request));
    }

    @Test
    void updateAvailability_ShouldThrowException_WhenNegativeSpots() {
        Parking parking = new Parking();
        parking.setId(1L);
        parking.setAvailableSpots(2);

        Mockito.when(parkingRepository.findById(1L)).thenReturn(Optional.of(parking));

        ParkingRequest request = ParkingRequest.builder()
                .parkingId(1L)
                .availableSpots(-1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> parkingService.updateAvailability(request));
    }

}