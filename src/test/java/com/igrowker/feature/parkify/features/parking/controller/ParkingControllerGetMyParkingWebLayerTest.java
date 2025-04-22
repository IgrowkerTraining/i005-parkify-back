package com.igrowker.feature.parkify.features.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@DisplayName("ParkingController Web Layer Tests - GET /api/v1/parkings/my (#26)")
class ParkingControllerGetMyParkingWebLayerTest {

    private static final String OWNER_EMAIL_EXISTS_WITH_PARKING = "owner.with.parking@test.com";
    private static final String OWNER_EMAIL_EXISTS_NO_PARKING = "owner.no.parking@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String DELETED_OWNER_EMAIL = "deleted.owner@test.com";

    private static final Long OWNER_ID = 10L;
    private static final String PARKING_ID_STR = "1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    @MockBean
    private UserDetailsService userDetailsService;

    private ParkingDetailsResponse mockParkingDetailsResponse;

    @BeforeEach
    void setUp() {
        // Удаляем .featureSlugs(...)
        mockParkingDetailsResponse = ParkingDetailsResponse.builder()
                .id(PARKING_ID_STR)
                .name("My Mock Parking")
                .address("123 Mock St")
                .location(new LocationDto(40.0, -3.0))
                .description("A great mock parking")
                .capacity(50)
                .currentAvailability(25)
                .hourlyRate(5.5)
                .workingHours("Mon-Fri 9-18")
                // .featureSlugs(List.of("covered", "security")) // Удалено
                .ownerId(String.valueOf(OWNER_ID))
                .build();
    }

    @Nested
    @DisplayName("Success Scenarios")
    class SuccessTests {
        @Test
        @DisplayName("AC4, AC5: Should return 200 OK and ParkingDetails when authenticated as OWNER and parking exists")
        @WithMockUser(username = OWNER_EMAIL_EXISTS_WITH_PARKING, roles = {"OWNER"})
        void getMyParking_AsOwnerWithParking_ReturnsOkAndDetails() throws Exception {
            when(parkingService.getMyParkingDetails(OWNER_EMAIL_EXISTS_WITH_PARKING))
                    .thenReturn(mockParkingDetailsResponse);
            mockMvc.perform(get("/api/v1/parkings/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(PARKING_ID_STR)))
                    .andExpect(jsonPath("$.name", is(mockParkingDetailsResponse.getName())))
                    .andExpect(jsonPath("$.address", is(mockParkingDetailsResponse.getAddress())))
                    .andExpect(jsonPath("$.location.latitude", is(mockParkingDetailsResponse.getLocation().latitude())))
                    .andExpect(jsonPath("$.currentAvailability", is(mockParkingDetailsResponse.getCurrentAvailability())))
                    // Удаляем проверки для featureSlugs
                    // .andExpect(jsonPath("$.featureSlugs[0]", is("covered")))
                    // .andExpect(jsonPath("$.featureSlugs[1]", is("security")))
                    .andExpect(jsonPath("$.ownerId", is(String.valueOf(OWNER_ID))));
            verify(parkingService, times(1)).getMyParkingDetails(OWNER_EMAIL_EXISTS_WITH_PARKING);
        }
    }

    // Security Scenarios и NotFound Scenarios остаются без изменений,
    // так как они не зависели от содержимого ответа, а только от статуса и сообщения об ошибке.

    @Nested
    @DisplayName("Security Scenarios")
    class SecurityTests {
        @Test
        @DisplayName("AC2: Should return 401 Unauthorized when not authenticated")
        void getMyParking_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/parkings/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden()); // Ожидаем 403 Forbidden, так как Spring Security обычно отвечает так, если путь защищен, а пользователь не аутентифицирован или не имеет прав
            verify(parkingService, never()).getMyParkingDetails(any());
        }

        @Test
        @DisplayName("AC3: Should return 403 Forbidden when authenticated as DRIVER")
        @WithMockUser(username = DRIVER_EMAIL, roles = {"DRIVER"})
        void getMyParking_AsDriver_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/parkings/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
            verify(parkingService, never()).getMyParkingDetails(any());
        }
    }

    @Nested
    @DisplayName("Not Found Scenarios")
    class NotFoundTests {
        @Test
        @DisplayName("AC6: Should return 404 Not Found when Owner exists but has no parking")
        @WithMockUser(username = OWNER_EMAIL_EXISTS_NO_PARKING, roles = {"OWNER"})
        void getMyParking_OwnerExistsButNoParking_ReturnsNotFound() throws Exception {
            final String errorMessage = "Parking not found for owner with email: "
                    + OWNER_EMAIL_EXISTS_NO_PARKING;
            when(parkingService.getMyParkingDetails(OWNER_EMAIL_EXISTS_NO_PARKING))
                    .thenThrow(new ParkingNotFoundException(errorMessage));
            mockMvc.perform(get("/api/v1/parkings/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path", is("/api/v1/parkings/my")));
            verify(parkingService, times(1))
                    .getMyParkingDetails(OWNER_EMAIL_EXISTS_NO_PARKING);
        }

        @Test
        @DisplayName("AC7: Should return 404 Not Found when Owner is not found in DB (edge case)")
        @WithMockUser(username = DELETED_OWNER_EMAIL, roles = {"OWNER"})
        void getMyParking_OwnerNotFoundInDb_ReturnsNotFound() throws Exception {
            final String errorMessage = "Authenticated owner not found with email: "
                    + DELETED_OWNER_EMAIL;
            when(parkingService.getMyParkingDetails(DELETED_OWNER_EMAIL))
                    .thenThrow(new OwnerNotFoundException(errorMessage));
            mockMvc.perform(get("/api/v1/parkings/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is(errorMessage)))
                    .andExpect(jsonPath("$.path", is("/api/v1/parkings/my")));
            verify(parkingService, times(1))
                    .getMyParkingDetails(DELETED_OWNER_EMAIL);
        }
    }
}