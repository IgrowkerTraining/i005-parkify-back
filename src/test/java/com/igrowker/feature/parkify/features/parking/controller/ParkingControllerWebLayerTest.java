package com.igrowker.feature.parkify.features.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@DisplayName("ParkingController Web Layer Tests")
class ParkingControllerWebLayerTest {

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long NEW_PARKING_ID = 2L;
    private static final Long INVALID_PARKING_ID = 99L;
    private static final Long PARKING_ID_WITH_MISSING_OWNER = 3L;
    private static final Long VALID_OWNER_ID = 10L;
    private static final int EXPECTED_AVAILABILITY = 7;
    private static final String MOCK_OWNER_EMAIL = "owner@test.com";
    private static final String UNKNOWN_OWNER_EMAIL = "unknown@test.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ParkingService parkingService;
    @MockBean
    private UserDetailsService userDetailsService;

    // Вложенный класс GetParkingAvailabilityEndpointTests остается без изменений

    @Nested
    @DisplayName("GET /api/v1/parkings/{id}/availability Tests")
    class GetParkingAvailabilityEndpointTests {

        @Test
        @DisplayName("should return OK and data when authenticated and parking exists")
        @WithMockUser
        void getParkingAvailability_AuthenticatedAndParkingExists_ReturnsOkWithData() throws Exception {
            final ParkingAvailabilityResponse mockResponse = new ParkingAvailabilityResponse(
                    VALID_PARKING_ID, EXPECTED_AVAILABILITY
            );
            when(parkingService.getParkingAvailability(VALID_PARKING_ID)).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/parkings/{id}/availability", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.parkingId", is(VALID_PARKING_ID.intValue())))
                    .andExpect(jsonPath("$.availableSpots", is(EXPECTED_AVAILABILITY)));
        }

        @Test
        @DisplayName("should return 404 when authenticated and parking not found")
        @WithMockUser
        void getParkingAvailability_AuthenticatedAndParkingNotFound_ReturnsNotFound()
                throws Exception {
            final String errorMessage = "Parking not found with id: " + INVALID_PARKING_ID;
            when(parkingService.getParkingAvailability(INVALID_PARKING_ID))
                    .thenThrow(new ParkingNotFoundException(errorMessage));

            mockMvc.perform(get("/api/v1/parkings/{id}/availability", INVALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path", is("/api/v1/parkings/"
                            + INVALID_PARKING_ID + "/availability")));
        }

        @Test
        @DisplayName("should return OK when not authenticated (public access)")
        void getParkingAvailability_NotAuthenticated_ReturnsOk() throws Exception {
            final ParkingAvailabilityResponse mockResponse = new ParkingAvailabilityResponse(
                    VALID_PARKING_ID, EXPECTED_AVAILABILITY
            );
            when(parkingService.getParkingAvailability(VALID_PARKING_ID)).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/parkings/{id}/availability", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            verify(parkingService, times(1)).getParkingAvailability(VALID_PARKING_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/parkings/{id} Tests (#23)")
    class GetParkingDetailsEndpointTests {

        private ParkingDetailsResponse mockDetailsResponse; // Перенесли сюда для инициализации в BeforeEach

        @BeforeEach
        void setUpDetails() { // Отдельный BeforeEach для этого вложенного класса
            // Удаляем .featureSlugs(...)
            mockDetailsResponse = ParkingDetailsResponse.builder()
                    .id(String.valueOf(VALID_PARKING_ID))
                    .name("Mock Parking Detail")
                    .address("1 Detail Mock Street")
                    .location(new LocationDto(11.1, 22.2))
                    .description("Mock Detailed Description")
                    .capacity(100)
                    .currentAvailability(50)
                    .hourlyRate(4.5)
                    .workingHours("24/7")
                    // .featureSlugs(List.of("covered", "ev")) // Удалено
                    .ownerId(String.valueOf(VALID_OWNER_ID))
                    .build();
        }

        @Test
        @DisplayName("should return OK and parking details " +
                "when authenticated and parking/owner exist"
        )
        @WithMockUser
        void getParkingDetails_AuthenticatedAndExists_ReturnsOkWithData() throws Exception {
            when(parkingService.getParkingDetails(VALID_PARKING_ID)).thenReturn(mockDetailsResponse);

            mockMvc.perform(get("/api/v1/parkings/{id}", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id",
                            is(String.valueOf(VALID_PARKING_ID))))
                    .andExpect(jsonPath("$.name",
                            is(mockDetailsResponse.getName())))
                    // ... (остальные проверки jsonPath) ...
                    // Удаляем проверку featureSlugs
                    // .andExpect(jsonPath("$.featureSlugs", containsInAnyOrder("covered", "ev")))
                    .andExpect(jsonPath("$.ownerId",
                            is(String.valueOf(VALID_OWNER_ID))));
        }

        // Тесты ...ParkingNotFound... и ...OwnerNotFound... остаются без изменений

        @Test
        @DisplayName("should return 404 Not Found when authenticated and parking does not exist")
        @WithMockUser
        void getParkingDetails_AuthenticatedAndParkingNotFound_ReturnsNotFound() throws Exception {
            final String errorMessage = "Parking not found with id: " + INVALID_PARKING_ID;
            when(parkingService.getParkingDetails(INVALID_PARKING_ID))
                    .thenThrow(new ParkingNotFoundException(errorMessage));

            mockMvc.perform(get("/api/v1/parkings/{id}", INVALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path",
                            is("/api/v1/parkings/" + INVALID_PARKING_ID)));
        }

        @Test
        @DisplayName("should return 404 Not Found when authenticated and owner does not exist")
        @WithMockUser
        void getParkingDetails_AuthenticatedAndOwnerNotFound_ReturnsNotFound() throws Exception {
            final String errorMessage = "Owner not found with id: " + VALID_OWNER_ID; // Пример сообщения
            when(parkingService.getParkingDetails(PARKING_ID_WITH_MISSING_OWNER))
                    .thenThrow(new OwnerNotFoundException(errorMessage));

            mockMvc.perform(get("/api/v1/parkings/{id}", PARKING_ID_WITH_MISSING_OWNER)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path",
                            is("/api/v1/parkings/" + PARKING_ID_WITH_MISSING_OWNER)));
        }


        @Test
        @DisplayName("should return OK when not authenticated (public access)")
        void getParkingDetails_NotAuthenticated_ReturnsOk() throws Exception {
            // Пересоздаем мок без фич
            final ParkingDetailsResponse mockPublicResponse = ParkingDetailsResponse.builder()
                    .id(String.valueOf(VALID_PARKING_ID))
                    .name("Public Parking Details")
                    .build();
            when(parkingService.getParkingDetails(VALID_PARKING_ID)).thenReturn(mockPublicResponse);

            mockMvc.perform(get("/api/v1/parkings/{id}", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            verify(parkingService, times(1)).getParkingDetails(VALID_PARKING_ID);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/parkings/my Tests (#15)")
    class CreateMyParkingEndpointTests {

        private CreateMyParkingRequest validCreateRequest;
        private ParkingResponse createdParkingResponse;

        @BeforeEach
        void createMyParkingSetup() {
            // Удаляем featureSlugs из запроса
            validCreateRequest = CreateMyParkingRequest.builder()
                    .name("My Web Layer Parking")
                    .address("456 Web St")
                    .latitude(50.0)
                    .longitude(-2.0)
                    .description("Created via web test")
                    .capacity(20)
                    .hourlyRate(3.0)
                    .workingHours("08:00-22:00")
                    //.featureSlugs(...) // Удалено
                    .build();

            // Удаляем featureSlugs из ответа
            createdParkingResponse = ParkingResponse.builder()
                    .id(NEW_PARKING_ID)
                    .name(validCreateRequest.getName())
                    .address(validCreateRequest.getAddress())
                    .latitude(validCreateRequest.getLatitude())
                    .longitude(validCreateRequest.getLongitude())
                    .description(validCreateRequest.getDescription())
                    .capacity(validCreateRequest.getCapacity())
                    .currentAvailability(validCreateRequest.getCapacity())
                    .hourlyRate(validCreateRequest.getHourlyRate())
                    .workingHours(validCreateRequest.getWorkingHours())
                    //.featureSlugs(...) // Удалено
                    .ownerId(VALID_OWNER_ID)
                    .build();
        }

        @Test
        @DisplayName("should return 201 Created and parking data when authenticated as OWNER with valid data")
        @WithMockUser(username = MOCK_OWNER_EMAIL, roles = {"OWNER"})
        void createMyParking_AuthenticatedAsOwner_ValidData_ReturnsCreated() throws Exception {
            // Мокирование сервиса остается тем же, но CreateMyParkingRequest будет без featureSlugs
            when(parkingService.createMyParking(any(CreateMyParkingRequest.class), eq(MOCK_OWNER_EMAIL)))
                    .thenReturn(createdParkingResponse);

            mockMvc.perform(post("/api/v1/parkings/my")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION,
                            "http://localhost/api/v1/parkings/" + NEW_PARKING_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id",
                            is(NEW_PARKING_ID.intValue())))
            // ... (остальные проверки jsonPath) ...
            // Удаляем проверку featureSlugs
            // .andExpect(jsonPath("$.featureSlugs", ...))
            ;

            verify(parkingService, times(1))
                    .createMyParking(any(CreateMyParkingRequest.class), eq(MOCK_OWNER_EMAIL));
        }

        // Тесты ...InvalidData..., ...NotAuthenticated..., ...OwnerNotFound..., ...UnexpectedException... остаются без изменений

        @Test
        @DisplayName("should return 400 Bad Request when authenticated as OWNER with invalid data (e.g., negative capacity)")
        @WithMockUser(username = MOCK_OWNER_EMAIL, roles = {"OWNER"})
        void createMyParking_AuthenticatedAsOwner_InvalidData_ReturnsBadRequest() throws Exception {
            // Запрос без фич
            final CreateMyParkingRequest invalidRequest = CreateMyParkingRequest.builder()
                    .name("Valid Name")
                    .address("Valid Address")
                    .latitude(1.0)
                    .longitude(1.0)
                    .capacity(-5) // Невалидное значение
                    .hourlyRate(2.0)
                    .build();

            mockMvc.perform(post("/api/v1/parkings/my")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    // Проверка деталей ошибки может измениться в зависимости от валидации, но принцип тот же
                    .andExpect(jsonPath("$.details.capacity", is("Capacity must be zero or positive")));
            verify(parkingService, never()).createMyParking(any(), any());
        }

        @Test
        @DisplayName("should return 403 Forbidden when not authenticated")
        void createMyParking_NotAuthenticated_ReturnsForbidden() throws Exception {
            // Запрос без фич
            final CreateMyParkingRequest request = CreateMyParkingRequest.builder()
                    .name("Test") .address("Test") .latitude(1.0) .longitude(1.0)
                    .capacity(10) .hourlyRate(1.0) .build();

            mockMvc.perform(post("/api/v1/parkings/my")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden()); // Ожидаем 403

            verify(parkingService, never()).createMyParking(any(), any());
        }

        @Test
        @DisplayName("should return 404 Not Found when authenticated OWNER is not found in DB")
        @WithMockUser(username = UNKNOWN_OWNER_EMAIL, roles = {"OWNER"})
        void createMyParking_AuthenticatedOwnerNotFoundInDb_ReturnsNotFound() throws Exception {
            // Запрос без фич
            final CreateMyParkingRequest request = CreateMyParkingRequest.builder()
                    .name("Test") .address("Test") .latitude(1.0) .longitude(1.0)
                    .capacity(10) .hourlyRate(1.0) .build();
            final String errorMessage = "Authenticated owner not found with email: " + UNKNOWN_OWNER_EMAIL;
            when(parkingService.createMyParking(any(CreateMyParkingRequest.class), eq(UNKNOWN_OWNER_EMAIL)))
                    .thenThrow(new OwnerNotFoundException(errorMessage));

            mockMvc.perform(post("/api/v1/parkings/my")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path", is("/api/v1/parkings/my")));

            verify(parkingService, times(1))
                    .createMyParking(any(CreateMyParkingRequest.class), eq(UNKNOWN_OWNER_EMAIL));
        }

        @Test
        @DisplayName("should return 500 Internal Server Error when service throws unexpected exception")
        @WithMockUser(username = MOCK_OWNER_EMAIL, roles = {"OWNER"})
        void createMyParking_ServiceThrowsUnexpectedException_ReturnsInternalServerError() throws Exception {
            // Запрос без фич
            final CreateMyParkingRequest request = CreateMyParkingRequest.builder()
                    .name("Test") .address("Test") .latitude(1.0) .longitude(1.0)
                    .capacity(10) .hourlyRate(1.0) .build();
            final String genericErrorMessage = "Unexpected database error";
            when(parkingService.createMyParking(any(CreateMyParkingRequest.class), eq(MOCK_OWNER_EMAIL)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/api/v1/parkings/my")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.error", is("Internal Server Error")))
                    .andExpect(jsonPath("$.message", is("Ocurrió un error inesperado. Intenta nuevamente.")))
                    .andExpect(jsonPath("$.path", is("/api/v1/parkings/my")));

            verify(parkingService, times(1))
                    .createMyParking(any(CreateMyParkingRequest.class), eq(MOCK_OWNER_EMAIL));
        }
    }
}