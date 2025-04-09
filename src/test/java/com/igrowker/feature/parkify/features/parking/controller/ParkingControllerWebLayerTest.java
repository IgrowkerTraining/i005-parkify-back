package com.igrowker.feature.parkify.features.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
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

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@DisplayName("ParkingController Web Layer Tests")
class ParkingControllerWebLayerTest {

    private static final Long VALID_PARKING_ID = 1L;
    private static final Long INVALID_PARKING_ID = 99L;
    private static final Long PARKING_ID_WITH_MISSING_OWNER = 2L;
    private static final Long VALID_OWNER_ID = 10L;
    private static final int EXPECTED_AVAILABILITY = 7;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ParkingService parkingService;
    @MockBean
    private UserDetailsService userDetailsService;

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
        void getParkingAvailability_AuthenticatedAndParkingNotFound_ReturnsNotFound() throws Exception {
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
        @DisplayName("should return 403 Forbidden when not authenticated")
        void getParkingAvailability_NotAuthenticated_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/parkings/{id}/availability", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/parkings/{id} Tests (#23)")
    class GetParkingDetailsEndpointTests {

        @Test
        @DisplayName("should return OK and parking details " +
                "when authenticated and parking/owner exist"
        )
        @WithMockUser
        void getParkingDetails_AuthenticatedAndExists_ReturnsOkWithData() throws Exception {
            final ParkingResponse mockResponse = ParkingResponse.builder()
                    .id(VALID_PARKING_ID)
                    .name("Mock Parking Detail")
                    .address("1 Detail Mock Street")
                    .latitude(11.1)
                    .longitude(22.2)
                    .description("Mock Detailed Description")
                    .capacity(100)
                    .currentAvailability(50)
                    .hourlyRate(4.5)
                    .workingHours("24/7")
                    .features(List.of("covered", "ev"))
                    .ownerId(VALID_OWNER_ID)
                    .build();
            when(parkingService.getParkingDetails(VALID_PARKING_ID)).thenReturn(mockResponse);

            mockMvc.perform(get("/api/v1/parkings/{id}", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(VALID_PARKING_ID.intValue())))
                    .andExpect(jsonPath("$.name", is(mockResponse.getName())))
                    .andExpect(jsonPath("$.address", is(mockResponse.getAddress())))
                    .andExpect(jsonPath("$.latitude", is(mockResponse.getLatitude())))
                    .andExpect(jsonPath("$.longitude", is(mockResponse.getLongitude())))
                    .andExpect(jsonPath("$.description", is(mockResponse.getDescription())))
                    .andExpect(jsonPath("$.capacity", is(mockResponse.getCapacity())))
                    .andExpect(jsonPath("$.currentAvailability", is(mockResponse.getCurrentAvailability())))
                    .andExpect(jsonPath("$.hourlyRate", is(mockResponse.getHourlyRate())))
                    .andExpect(jsonPath("$.workingHours", is(mockResponse.getWorkingHours())))
                    .andExpect(jsonPath("$.features", containsInAnyOrder("covered", "ev")))
                    .andExpect(jsonPath("$.ownerId", is(VALID_OWNER_ID.intValue())))
                    .andExpect(jsonPath("$.contactPhone").doesNotExist());
        }

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
            final String errorMessage = "Owner not found with id: " + VALID_OWNER_ID
                    + " for parking id: " + PARKING_ID_WITH_MISSING_OWNER;
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
        @DisplayName("should return 403 Forbidden when not authenticated")
        void getParkingDetails_NotAuthenticated_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/parkings/{id}", VALID_PARKING_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}