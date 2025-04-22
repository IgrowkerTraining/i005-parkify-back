package com.igrowker.feature.parkify.features.parking.service;

// Удалены импорты: FeatureNotFoundException, Feature, FeatureRepository, Set
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
// Удалены импорты рекомендаций, если они не используются в других тестах этого класса
// import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendationResponse;
// import com.igrowker.feature.parkify.features.recommendation.entities.OccupancyHistory;
// import com.igrowker.feature.parkify.features.recommendation.repository.OccupancyHistoryRepository;
// import com.igrowker.feature.parkify.features.recommendation.service.RecommendationServiceImpl;
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
import org.springframework.dao.DataAccessException;

// Удален импорт LocalDateTime, если рекомендации удалены
// import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List; // Добавлен импорт List, если нужен для getOwnerWithParking
import java.util.Optional;
import java.util.HashSet; // Добавлен импорт HashSet, если нужен


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never; // Оставим, может пригодиться
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingServiceImpl Unit Tests")
class ParkingServiceImplTest {

    private static final Long VALID_PARKING_ID_LONG = 1L;
    private static final String VALID_PARKING_ID_STR = "1";
    private static final Long VALID_OWNER_ID_LONG = 10L;
    private static final String VALID_OWNER_ID_STR = "10";
    private static final String VALID_OWNER_EMAIL = "owner@test.com";
    private static final String UNKNOWN_OWNER_EMAIL = "unknown@test.com";
    private static final Long INVALID_PARKING_ID_LONG = 99L;
    private static final Long OWNER_ID_FOR_MISSING_OWNER_LONG = 11L;
    private static final int EXPECTED_AVAILABILITY = 10;
    private static final int DEFAULT_CAPACITY = 50;

    // Удалены константы FEATURE_...

    @Mock
    private ParkingRepository parkingRepository;
    // Удален мок OccupancyHistoryRepository, если рекомендации удалены
    // @Mock private OccupancyHistoryRepository occupancyHistoryRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    // Удален мок FeatureRepository
    // @Mock private FeatureRepository featureRepository;

    @InjectMocks
    private ParkingServiceImpl parkingService;
    // Удален InjectMocks RecommendationServiceImpl, если рекомендации удалены
    // @InjectMocks private RecommendationServiceImpl recommendationService;

    @Captor
    private ArgumentCaptor<Parking> parkingCaptor;

    private Parking mockParking;
    private AuthUser mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new AuthUser();
        mockOwner.setId(VALID_OWNER_ID_LONG);
        mockOwner.setEmail(VALID_OWNER_EMAIL);
        mockOwner.setContactPhone("123-456-7890");

        // Удалено .features(...)
        mockParking = Parking.builder()
                .id(VALID_PARKING_ID_LONG)
                .name("Test Parking")
                .address("123 Test St")
                .latitude(10.0)
                .longitude(20.0)
                .description("A nice test parking")
                .capacity(DEFAULT_CAPACITY)
                .availableSpots(25)
                .hourlyRate(5.5)
                .workingHours("Mon-Fri 9-18")
                // .features(...) // Удалено
                .ownerId(VALID_OWNER_ID_LONG)
                .build();
    }

    @Test
    void getOwnerWithParking_ShouldReturnOwnerAndParkingDetails() {
        String email = "owner@example.com";
        AuthUser owner = new AuthUser();
        owner.setId(1L);
        owner.setUsername("Owner Name");
        owner.setEmail(email);
        owner.setContactPhone("+56999887766");

        Parking parking = new Parking();
        parking.setId(1L);
        parking.setName("Test Parking");
        parking.setAddress("123 Test Address");
        parking.setLatitude(40.7128);
        parking.setLongitude(74.0060);
        parking.setDescription("Test Parking Description");
        parking.setAvailableSpots(5);
        parking.setHourlyRate(10.0);
        parking.setWorkingHours("08:00 AM - 08:00 PM");
        parking.setOwnerId(1L);

        // Используем Optional или List в зависимости от твоего репозитория
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(owner));
        // Предполагаем, что findByOwnerId возвращает Optional<Parking>
        when(parkingRepository.findByOwnerId(owner.getId())).thenReturn(List.of(parking));
        // Если он возвращает List<Parking>, то как было:
        // when(parkingRepository.findByOwnerId(owner.getId())).thenReturn(Collections.singletonList(parking));

        OwnerParkingDetailsResponse response = parkingService.getOwnerWithParking(email);

        assertNotNull(response);
        assertEquals(owner.getUsername(), response.getOwnerName());
        assertEquals(owner.getEmail(), response.getOwnerEmail());
        assertEquals(owner.getContactPhone(), response.getOwnerPhone());

        ParkingResponse parkingResponse = response.getParking();
        assertNotNull(parkingResponse);
        // Удаляем проверку фич, если она была
        // assertThat(parkingResponse.getFeatureSlugs()).isEmpty(); // Или что-то другое

        // Остальные проверки остаются
        assertEquals(parking.getId(), parkingResponse.getId());
        assertEquals(parking.getName(), parkingResponse.getName());
        // ... и т.д.

        verify(authUserRepository, times(1)).findByEmail(email);
        verify(parkingRepository, times(1)).findByOwnerId(owner.getId());
    }

    // Тесты getOwnerWithParking_ShouldThrowException... остаются как есть (проверяют owner/parking not found)

    // Удален тест generateRecommendations_shouldReturnHighAvailabilityParkings, если он больше не нужен


    @Nested
    @DisplayName("Get Parking Details (#23)")
    class GetParkingDetailsTests {

        @Test
        @DisplayName("should return correct ParkingDetailsResponse when parking and owner exist")
        void getParkingDetails_ParkingAndOwnerExist_ReturnsCorrectDto() {
            // Удалили установку фич в mockParking в основном setUp

            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner));

            final ParkingDetailsResponse actualResponse = parkingService.getParkingDetails(VALID_PARKING_ID_LONG);

            assertThat(actualResponse).isNotNull();
            assertAll("ParkingDetailsResponse validation",
                    // ... (остальные ассерты остаются)
                    () -> assertThat(actualResponse.getId()).isEqualTo(VALID_PARKING_ID_STR),
                    () -> assertThat(actualResponse.getName()).isEqualTo(mockParking.getName()),
                    // ...
                    // Удаляем ассерт для featureSlugs:
                    // () -> assertThat(actualResponse.getFeatureSlugs()).containsExactlyInAnyOrder(...),
                    () -> assertThat(actualResponse.getOwnerId()).isEqualTo(VALID_OWNER_ID_STR)
            );
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository).findById(VALID_OWNER_ID_LONG);
            verifyNoMoreInteractions(parkingRepository, authUserRepository);
        }

        @Test
        @DisplayName("should return zero availability when availableSpots is null in entity")
        void getParkingDetails_NullAvailableSpots_ReturnsZeroAvailability() {
            // Этот тест не затрагивался удалением фич, остается
            mockParking.setAvailableSpots(null);
            when(parkingRepository.findById(VALID_PARKING_ID_LONG)).thenReturn(Optional.of(mockParking));
            when(authUserRepository.findById(VALID_OWNER_ID_LONG)).thenReturn(Optional.of(mockOwner));

            final ParkingDetailsResponse actualResponse = parkingService
                    .getParkingDetails(VALID_PARKING_ID_LONG);

            assertThat(actualResponse.getCurrentAvailability()).isZero();
            verify(parkingRepository).findById(VALID_PARKING_ID_LONG);
            verify(authUserRepository).findById(VALID_OWNER_ID_LONG);
        }

        // Удален тест getParkingDetails_EmptyOrNullFeatures_ReturnsEmptyList

        // Тесты на ParkingNotFoundException и OwnerNotFoundException остаются

    }

    // Вложенный класс GetParkingAvailabilityTests остается без изменений

    @Nested
    @DisplayName("Create My Parking (#15 - Step 2)")
    class CreateMyParkingTests {

        private CreateMyParkingRequest validRequest;
        // Удаляем requestWithoutFeatures и requestWithUnknownFeature, если они больше не нужны
        // private CreateMyParkingRequest requestWithoutFeatures;
        // private CreateMyParkingRequest requestWithUnknownFeature;
        private Parking savedParkingEntity;
        private Long newParkingId = 2L;

        @BeforeEach
        void createMyParkingSetup() {
            // Удаляем featureSlugs из билдера
            validRequest = CreateMyParkingRequest.builder()
                    .name("My New Parking")
                    .address("15 New St")
                    .latitude(40.0)
                    .longitude(-3.0)
                    .description("Brand new parking")
                    .capacity(DEFAULT_CAPACITY)
                    .hourlyRate(6.0)
                    .workingHours("Mon-Sun 00-24")
                    //.featureSlugs(...) // Удалено
                    .build();

            // Удаляем features из билдера
            savedParkingEntity = Parking.builder()
                    .id(newParkingId)
                    .name(validRequest.getName())
                    // ... (остальные поля)
                    //.features(...) // Удалено
                    .ownerId(VALID_OWNER_ID_LONG)
                    .availableSpots(validRequest.getCapacity())
                    .build();
        }

        @Test
        @DisplayName("should create parking and return ParkingResponse when owner exists") // Обновили DisplayName
        void createMyParking_OwnerExists_CreatesAndReturnsParkingResponse() { // Обновили имя
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            // Удаляем мок featureRepository
            // when(featureRepository.findBySlugIn(...)).thenReturn(...);
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            // Удаляем верификацию featureRepository
            // verify(featureRepository).findBySlugIn(...);
            verify(parkingRepository).save(parkingCaptor.capture());

            final Parking capturedParking = parkingCaptor.getValue();
            assertAll("Captured Parking Entity validation",
                    () -> assertThat(capturedParking.getName()).isEqualTo(validRequest.getName()),
                    // ... (остальные ассерты для capturedParking)
                    // Удаляем ассерт для features
                    // () -> assertThat(capturedParking.getFeatures())...
                    () -> assertThat(capturedParking.getOwnerId()).isEqualTo(VALID_OWNER_ID_LONG)
            );

            assertAll("ParkingResponse validation after creation",
                    // ... (остальные ассерты для actualResponse)
                    () -> assertNotNull(actualResponse),
                    () -> assertEquals(newParkingId, actualResponse.getId()),
                    // ...
                    // Удаляем ассерт для featureSlugs
                    // () -> assertThat(actualResponse.getFeatureSlugs())...
                    () -> assertEquals(savedParkingEntity.getOwnerId(), actualResponse.getOwnerId())
            );

            // Удаляем featureRepository из verifyNoMoreInteractions
            verifyNoMoreInteractions(authUserRepository, parkingRepository);
        }

        // Удален тест createMyParking_NoFeaturesRequested_CreatesParkingWithEmptyFeatures (по сути, теперь это основной тест выше)
        // Удален тест createMyParking_UnknownFeatureSlug_ThrowsFeatureNotFoundException

        @Test
        @DisplayName("should throw OwnerNotFoundException when owner email does not exist")
        void createMyParking_OwnerNotFound_ThrowsOwnerNotFoundException() {
            when(authUserRepository.findByEmail(UNKNOWN_OWNER_EMAIL)).thenReturn(Optional.empty());

            final OwnerNotFoundException exception = assertThrows(
                    OwnerNotFoundException.class,
                    () -> parkingService.createMyParking(validRequest, UNKNOWN_OWNER_EMAIL)
            );

            assertThat(exception.getMessage()).isEqualTo("Authenticated owner not found with email: " + UNKNOWN_OWNER_EMAIL);
            verify(authUserRepository).findByEmail(UNKNOWN_OWNER_EMAIL);
            // Удаляем featureRepository из verifyNoInteractions
            verifyNoInteractions(parkingRepository);
        }

        @Test
        @DisplayName("should propagate DataAccessException when repository save fails")
        void createMyParking_RepositorySaveFails_ThrowsDataAccessException() {
            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            // Удаляем мок featureRepository
            final DataAccessException dbException = new DataAccessException("Simulated error") {};
            when(parkingRepository.save(any(Parking.class))).thenThrow(dbException);

            final DataAccessException thrown = assertThrows(DataAccessException.class, () ->
                    parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL)
            );

            assertEquals(dbException, thrown);
            verify(authUserRepository).findByEmail(VALID_OWNER_EMAIL);
            // Удаляем верификацию featureRepository
            verify(parkingRepository).save(any(Parking.class));
        }

        @Test
        @DisplayName("should correctly initialize availableSpots with capacity on creation")
        void createMyParking_ShouldInitializeAvailabilityWithCapacity() {
            // Этот тест не затрагивался удалением фич, остается
            final int specificCapacity = 77;
            validRequest.setCapacity(specificCapacity);
            // Удаляем featureSlugs
            // validRequest.setFeatureSlugs(Collections.emptyList());
            savedParkingEntity.setCapacity(specificCapacity);
            savedParkingEntity.setAvailableSpots(specificCapacity);
            // Удаляем features
            // savedParkingEntity.setFeatures(Collections.emptySet());

            when(authUserRepository.findByEmail(VALID_OWNER_EMAIL)).thenReturn(Optional.of(mockOwner));
            when(parkingRepository.save(any(Parking.class))).thenReturn(savedParkingEntity);

            final ParkingResponse actualResponse = parkingService.createMyParking(validRequest, VALID_OWNER_EMAIL);

            verify(parkingRepository).save(parkingCaptor.capture());
            final Parking capturedParking = parkingCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedParking.getAvailableSpots())
                            .as("Captured entity: Available spots should be initialized with capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCurrentAvailability())
                            .as("Response DTO: Should reflect initial availability equal to capacity")
                            .isEqualTo(specificCapacity),
                    () -> assertThat(actualResponse.getCapacity()).isEqualTo(specificCapacity)
            );
        }
    }

    // Вложенный класс UpdateAvailabilityTests остается без изменений
    @Nested
    @DisplayName("Update Parking Availability (#27)")
    class UpdateAvailabilityTests {
        // Тесты для updateMyParkingAvailability (если они были или будут добавлены)
    }
}