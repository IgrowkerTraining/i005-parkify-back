package com.igrowker.feature.parkify.features.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import org.junit.jupiter.api.DisplayName;
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
    private static final int EXPECTED_AVAILABILITY = 7;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ParkingService parkingService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/parkings/{id}/availability should return OK and data " +
            "when authenticated and parking exists"
    )
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
    @DisplayName("GET /api/v1/parkings/{id}/availability should return 404 " +
            "when authenticated and parking not found"
    )
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
    @DisplayName("GET /api/v1/parkings/{id}/availability should return 401 Unauthorized" +
            " when not authenticated"
    )
    void getParkingAvailability_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/parkings/{id}/availability", VALID_PARKING_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}