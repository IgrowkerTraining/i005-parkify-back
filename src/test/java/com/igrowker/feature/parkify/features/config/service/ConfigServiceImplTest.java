package com.igrowker.feature.parkify.features.config.service;

import com.igrowker.feature.parkify.features.config.config.InitialConfigProperties;
import com.igrowker.feature.parkify.features.config.dto.InitialConfigResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat; // Используем AssertJ для удобных ассертов
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("ConfigServiceImpl Unit Tests")
class ConfigServiceImplTest {

    private ConfigServiceImpl configService;
    private static final String PRIMARY_COLOR = "#ABCDEF";
    private static final String SECONDARY_COLOR = "#123456";
    private static final boolean RECOMMENDATIONS_FLAG = true;
    private static final boolean BOOKING_FLAG = false;

    @BeforeEach
    void setUp() {
        final InitialConfigProperties testProperties = new InitialConfigProperties();
        final InitialConfigProperties.ThemeColorsProperties colors
                = new InitialConfigProperties.ThemeColorsProperties();
        colors.setPrimary(PRIMARY_COLOR);
        colors.setSecondary(SECONDARY_COLOR);
        testProperties.setThemeColors(colors);

        final InitialConfigProperties.FeatureFlagsProperties flags
                = new InitialConfigProperties.FeatureFlagsProperties();
        flags.setRecommendationsEnabled(RECOMMENDATIONS_FLAG);
        flags.setOnlineBookingEnabled(BOOKING_FLAG);
        testProperties.setFeatureFlags(flags);

        configService = new ConfigServiceImpl(testProperties);
    }

    @Test
    @DisplayName("getInitialConfigData should map properties to DTO correctly")
    void getInitialConfigData_shouldMapPropertiesToDtoCorrectly() {
        InitialConfigResponse actualResponse = configService.getInitialConfigData();

        assertAll(
                () -> assertThat(actualResponse.themeColors().primary())
                        .isEqualTo(PRIMARY_COLOR),
                () -> assertThat(actualResponse.themeColors().secondary())
                        .isEqualTo(SECONDARY_COLOR),
                () -> assertThat(actualResponse.featureFlags().recommendationsEnabled())
                        .isEqualTo(RECOMMENDATIONS_FLAG),
                () -> assertThat(actualResponse.featureFlags().onlineBookingEnabled())
                        .isEqualTo(BOOKING_FLAG)
        );
    }
}