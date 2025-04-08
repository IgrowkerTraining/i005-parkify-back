package com.igrowker.feature.parkify.features.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.config.dto.FeatureFlagsDto;
import com.igrowker.feature.parkify.features.config.dto.InitialConfigResponse;
import com.igrowker.feature.parkify.features.config.dto.ThemeColorsDto;
import com.igrowker.feature.parkify.features.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigController.class)
@Import({SecurityConfig.class, JwtService.class})
@DisplayName("ConfigController Web Layer Tests")
class ConfigControllerWebLayerTest {
    private static final String PRIMARY_COLOR = "#FEDCBA";
    private static final String SECONDARY_COLOR = "#987654";
    private static final boolean RECOMMENDATIONS_FLAG = false;
    private static final boolean BOOKING_FLAG = true;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ConfigService configService;
    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        final ThemeColorsDto themeColors = new ThemeColorsDto(PRIMARY_COLOR, SECONDARY_COLOR);
        final FeatureFlagsDto featureFlags = new FeatureFlagsDto(RECOMMENDATIONS_FLAG, BOOKING_FLAG);
        final InitialConfigResponse expectedResponse = new InitialConfigResponse(themeColors, featureFlags);

        when(configService.getInitialConfigData()).thenReturn(expectedResponse);
    }

    @Test
    @DisplayName("GET /api/v1/config/initial should return initial config data")
    void getInitialConfig_shouldReturnInitialConfigData() throws Exception {
        mockMvc.perform(get("/api/v1/config/initial")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.themeColors.primary",
                        is(PRIMARY_COLOR)))
                .andExpect(jsonPath("$.themeColors.secondary",
                        is(SECONDARY_COLOR)))
                .andExpect(jsonPath("$.featureFlags.recommendationsEnabled",
                        is(RECOMMENDATIONS_FLAG)))
                .andExpect(jsonPath("$.featureFlags.onlineBookingEnabled",
                        is(BOOKING_FLAG)));
    }
}