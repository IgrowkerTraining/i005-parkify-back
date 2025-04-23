package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingSummaryResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl - getMyParkingSummaries Unit Tests")
class ParkingServiceGetMyParkingSummariesTest {

    private static final String OWNER_EMAIL_EXISTS = "owner.summary@test.com";
    private static final String OWNER_EMAIL_NOT_FOUND = "owner.summary.notfound@test.com";
    private static final Long OWNER_ID = 1L;

    @Mock
    private ParkingRepository parkingRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @InjectMocks
    private ParkingServiceImpl parkingService;

    private AuthUser testOwner;
    private Parking parking1;
    private Parking parking2;
    private Parking parkingNullSpots;


    @BeforeEach
    void setUp() {
        testOwner = new AuthUser();
        testOwner.setId(OWNER_ID);
        testOwner.setEmail(OWNER_EMAIL_EXISTS);

        parking1 = Parking.builder()
                .id(10L).name("Parking 1").address("Addr 1").latitude(1.0).longitude(1.0)
                .availableSpots(5).hourlyRate(2.0).parkingPhone("111").parkingImageUrl("url1")
                .ownerId(OWNER_ID)
                .build();
        parking2 = Parking.builder()
                .id(20L).name("Parking 2").address("Addr 2").latitude(2.0).longitude(2.0)
                .availableSpots(10).hourlyRate(3.0).parkingPhone("222").parkingImageUrl("url2")
                .ownerId(OWNER_ID)
                .build();
        parkingNullSpots = Parking.builder()
                .id(30L).name("Parking Null Spots").address("Addr 3").latitude(3.0).longitude(3.0)
                .availableSpots(null).hourlyRate(4.0).parkingPhone("333").parkingImageUrl("url3")
                .ownerId(OWNER_ID)
                .build();
    }

    @Test
    @DisplayName("Should return list of summaries when owner exists and has parkings")
    void getMyParkingSummaries_OwnerExistsWithParkings_ShouldReturnSummaries() {
        List<Parking> ownerParkings = List.of(parking1, parking2, parkingNullSpots);
        when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
        when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(ownerParkings);

        List<ParkingSummaryResponse> result = parkingService.getMyParkingSummaries(OWNER_EMAIL_EXISTS);

        assertThat(result).isNotNull().hasSize(3);

        ParkingSummaryResponse summary1 = result.stream().filter(s -> s.getId().equals("10")).findFirst().orElse(null);
        assertThat(summary1).isNotNull();
        assertAll("Summary 1 Details",
                () -> assertThat(summary1.getId()).isEqualTo("10"),
                () -> assertThat(summary1.getName()).isEqualTo(parking1.getName()),
                () -> assertThat(summary1.getAddress()).isEqualTo(parking1.getAddress()),
                () -> assertThat(summary1.getLocation().latitude()).isEqualTo(parking1.getLatitude()),
                () -> assertThat(summary1.getLocation().longitude()).isEqualTo(parking1.getLongitude()),
                () -> assertThat(summary1.getCurrentAvailability()).isEqualTo(parking1.getAvailableSpots()),
                () -> assertThat(summary1.getHourlyRate()).isEqualTo(parking1.getHourlyRate()),
                () -> assertThat(summary1.getParkingPhone()).isEqualTo(parking1.getParkingPhone()),
                () -> assertThat(summary1.getParkingImageUrl()).isEqualTo(parking1.getParkingImageUrl()),
                () -> assertThat(summary1.getDistance()).isNull()
        );

        ParkingSummaryResponse summary2 = result.stream().filter(s -> s.getId().equals("20")).findFirst().orElse(null);
        assertThat(summary2).isNotNull();
        assertThat(summary2.getCurrentAvailability()).isEqualTo(parking2.getAvailableSpots());

        ParkingSummaryResponse summary3 = result.stream().filter(s -> s.getId().equals("30")).findFirst().orElse(null);
        assertThat(summary3).isNotNull();
        assertThat(summary3.getCurrentAvailability()).isZero();

        verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_EXISTS);
        verify(parkingRepository, times(1)).findByOwnerId(OWNER_ID);
        verifyNoMoreInteractions(authUserRepository, parkingRepository);
    }

    @Test
    @DisplayName("Should return empty list when owner exists but has no parkings")
    void getMyParkingSummaries_OwnerExistsNoParkings_ShouldReturnEmptyList() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_EXISTS)).thenReturn(Optional.of(testOwner));
        when(parkingRepository.findByOwnerId(OWNER_ID)).thenReturn(Collections.emptyList());

        List<ParkingSummaryResponse> result = parkingService.getMyParkingSummaries(OWNER_EMAIL_EXISTS);

        assertThat(result).isNotNull().isEmpty();

        verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_EXISTS);
        verify(parkingRepository, times(1)).findByOwnerId(OWNER_ID);
        verifyNoMoreInteractions(authUserRepository, parkingRepository);
    }

    @Test
    @DisplayName("Should throw OwnerNotFoundException when owner email does not exist")
    void getMyParkingSummaries_OwnerNotFound_ShouldThrowOwnerNotFoundException() {
        when(authUserRepository.findByEmail(OWNER_EMAIL_NOT_FOUND)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.getMyParkingSummaries(OWNER_EMAIL_NOT_FOUND))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessageContaining("Authenticated owner not found with email: " + OWNER_EMAIL_NOT_FOUND);

        verify(authUserRepository, times(1)).findByEmail(OWNER_EMAIL_NOT_FOUND);
        verifyNoInteractions(parkingRepository);
        verifyNoMoreInteractions(authUserRepository);
    }
}
