package com.igrowker.feature.parkify.features.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import com.igrowker.feature.parkify.features.recommendation.repository.OccupancyHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration Tests for GET /api/v1/parkings (Nearby Search - Task #20 & #21)")
class ParkingControllerNearbySearchIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingRepository parkingRepository;
    @MockBean
    private AuthUserRepository authUserRepository;
    // Удален мок FeatureRepository
    // @MockBean private FeatureRepository featureRepository;
    @MockBean
    private OccupancyHistoryRepository occupancyHistoryRepository;

    @Configuration
    @ImportAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @ComponentScan(basePackages = "com.igrowker.feature.parkify")
    static class TestConfig {}

    private final double centerLat = 40.0;
    private final double centerLon = -3.0;
    private final double distanceErrorMargin = 0.1;

    private Parking parkingNear1;
    private Parking parkingNear2;
    private Parking parkingFar;
    private Parking parkingClosest;
    private Parking parkingSecondClosest;
    private Parking parkingNearNullAvail; // Добавим парковку с null availability для теста
    private Parking parkingNearNullPrice; // Добавим парковку с null price для теста

    @BeforeEach
    void setUp() {
        // Убрали .features(...) из всех билдеров
        parkingClosest = Parking.builder()
                .id(4L).name("Parking Closest").address("Addr 4")
                .latitude(40.05).longitude(-3.05).availableSpots(0).hourlyRate(7.0).capacity(20)
                .ownerId(104L).build(); // features удалено
        parkingSecondClosest = Parking.builder()
                .id(5L).name("Parking Second Closest").address("Addr 5")
                .latitude(40.08).longitude(-2.95).availableSpots(15).hourlyRate(10.0).capacity(40)
                .ownerId(105L).build(); // features удалено
        parkingNear1 = Parking.builder()
                .id(1L).name("Parking Near 1").address("Addr 1")
                .latitude(40.1).longitude(-3.0).availableSpots(10).hourlyRate(5.0).capacity(50)
                .ownerId(101L).build(); // features удалено
        parkingNear2 = Parking.builder()
                .id(2L).name("Parking Near 2").address("Addr 2")
                .latitude(40.2).longitude(-3.0).availableSpots(5).hourlyRate(6.0).capacity(30)
                .ownerId(102L).build(); // features удалено
        parkingFar = Parking.builder()
                .id(3L).name("Parking Far").address("Addr 3")
                .latitude(40.5).longitude(-3.0).availableSpots(20).hourlyRate(4.0).capacity(100)
                .ownerId(103L).build(); // features удалено
        parkingNearNullAvail = Parking.builder() // Бывший parking4
                .id(6L).name("Parking Near Null Avail").address("Addr 6") // Используем ID 6 для него
                .latitude(40.05).longitude(-3.05).availableSpots(null).hourlyRate(10.0).capacity(20)
                .ownerId(106L).build(); // features удалено
        parkingNearNullPrice = Parking.builder() // Бывший parking5
                .id(7L).name("Parking Near Null Price").address("Addr 7") // Используем ID 7 для него
                .latitude(40.08).longitude(-2.95).availableSpots(15).hourlyRate(null).capacity(40)
                .ownerId(107L).build(); // features удалено

        List<Parking> allMockParkings = List.of(
                parkingNear1, parkingFar, parkingNear2, parkingClosest,
                parkingSecondClosest, parkingNearNullAvail, parkingNearNullPrice // Добавили новые
        );
        when(parkingRepository.findAll()).thenReturn(allMockParkings);
    }

    // Тесты findNearbyParkings_BasicRequest_ReturnsOkAndSortedData,
    // findNearbyParkings_WithRadius_ReturnsFilteredAndSortedData,
    // findNearbyParkings_WithPagination_ReturnsCorrectSubset,
    // findNearbyParkings_NoMatches_ReturnsEmptyList,
    // findNearbyParkings_MissingLatitude_ReturnsBadRequest,
    // findNearbyParkings_MissingLongitude_ReturnsBadRequest
    // остаются БЕЗ ИЗМЕНЕНИЙ, так как они не зависели от фич.
    // НО! Нужно проверить ожидаемые ID и размеры списков, так как мы добавили 2 парковки

    @Test
    @DisplayName("AC1, AC2, AC5: Should return nearby parkings sorted by distance with correct data and pagination")
    void findNearbyParkings_BasicRequest_ReturnsOkAndSortedData() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("latitude", String.valueOf(centerLat))
                        .param("longitude", String.valueOf(centerLon))
                        // Добавляем параметры по умолчанию для limit/offset, как в контроллере
                        .param("limit", "20")
                        .param("offset", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(7))) // Ожидаем 7 парковок
                // Обновляем ожидаемый порядок ID
                .andExpect(jsonPath("$.data[0].id", is("4"))) // Closest
                .andExpect(jsonPath("$.data[1].id", is("6"))) // Near Null Avail (та же точка, что и 4)
                .andExpect(jsonPath("$.data[2].id", is("5"))) // Second Closest
                .andExpect(jsonPath("$.data[3].id", is("7"))) // Near Null Price (та же точка, что и 5)
                .andExpect(jsonPath("$.data[4].id", is("1"))) // Near 1
                .andExpect(jsonPath("$.data[5].id", is("2"))) // Near 2
                .andExpect(jsonPath("$.data[6].id", is("3"))) // Far
                // Обновляем проверки на distance, availability и pagination total
                .andExpect(jsonPath("$.data[0].name", is("Parking Closest")))
                .andExpect(jsonPath("$.data[0].currentAvailability", is(0)))
                .andExpect(jsonPath("$.data[0].distance", closeTo(7.00, distanceErrorMargin)))
                .andExpect(jsonPath("$.data[1].id", is("6"))) // Проверяем второй элемент
                .andExpect(jsonPath("$.data[1].currentAvailability", is(0))) // Null availability = 0
                .andExpect(jsonPath("$.data[1].distance", closeTo(7.00, distanceErrorMargin)))
                // ... (проверки для остальных можно добавить при необходимости)
                .andExpect(jsonPath("$.data[6].currentAvailability", is(20)))
                .andExpect(jsonPath("$.data[6].distance", closeTo(55.60, distanceErrorMargin)))
                .andExpect(jsonPath("$.pagination.offset", is(0)))
                .andExpect(jsonPath("$.pagination.limit", is(20)))
                .andExpect(jsonPath("$.pagination.total", is(7))); // Ожидаем 7 всего
    }

    @Test
    @DisplayName("AC4: Should filter parkings by radius and return sorted data with correct pagination")
    void findNearbyParkings_WithRadius_ReturnsFilteredAndSortedData() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("latitude", String.valueOf(centerLat))
                        .param("longitude", String.valueOf(centerLon))
                        .param("radius", "30")
                        // Добавляем параметры по умолчанию для limit/offset
                        .param("limit", "20")
                        .param("offset", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Обновляем ожидаемые размеры и ID
                .andExpect(jsonPath("$.data", hasSize(6))) // 4, 6, 5, 7, 1, 2
                .andExpect(jsonPath("$.data[0].id", is("4")))
                .andExpect(jsonPath("$.data[1].id", is("6")))
                .andExpect(jsonPath("$.data[2].id", is("5")))
                .andExpect(jsonPath("$.data[3].id", is("7")))
                .andExpect(jsonPath("$.data[4].id", is("1")))
                .andExpect(jsonPath("$.data[5].id", is("2")))
                .andExpect(jsonPath("$.data[6]").doesNotExist())
                .andExpect(jsonPath("$.pagination.offset", is(0)))
                .andExpect(jsonPath("$.pagination.limit", is(20)))
                .andExpect(jsonPath("$.pagination.total", is(6))); // Ожидаем 6 всего
    }

    @Test
    @DisplayName("AC6: Should apply pagination correctly and return the right slice")
    void findNearbyParkings_WithPagination_ReturnsCorrectSubset() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("latitude", String.valueOf(centerLat))
                        .param("longitude", String.valueOf(centerLon))
                        .param("limit", "2")
                        .param("offset", "2") // Смещение 2 (третий и четвертый элементы)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                // Обновляем ожидаемые ID для offset=2
                .andExpect(jsonPath("$.data[0].id", is("5"))) // Третий элемент
                .andExpect(jsonPath("$.data[0].distance", closeTo(9.86, distanceErrorMargin)))
                .andExpect(jsonPath("$.data[1].id", is("7"))) // Четвертый элемент
                .andExpect(jsonPath("$.data[1].distance", closeTo(9.86, distanceErrorMargin)))
                .andExpect(jsonPath("$.pagination.offset", is(2))) // Проверяем offset
                .andExpect(jsonPath("$.pagination.limit", is(2)))
                .andExpect(jsonPath("$.pagination.total", is(7))); // Общее количество
    }

    @Test
    @DisplayName("Should return empty list when radius filter excludes all parkings")
    void findNearbyParkings_NoMatches_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("latitude", String.valueOf(centerLat))
                        .param("longitude", String.valueOf(centerLon))
                        .param("radius", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pagination.offset", is(0)))
                .andExpect(jsonPath("$.pagination.limit", is(20)))
                .andExpect(jsonPath("$.pagination.total", is(0)));
    }

    @Test
    @DisplayName("Should return 400 Bad Request if latitude is missing")
    void findNearbyParkings_MissingLatitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("longitude", String.valueOf(centerLon))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Required parameter 'latitude' of type Double is missing")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request if longitude is missing")
    void findNearbyParkings_MissingLongitude_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/parkings")
                        .param("latitude", String.valueOf(centerLat))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Required parameter 'longitude' of type Double is missing")));
    }
}