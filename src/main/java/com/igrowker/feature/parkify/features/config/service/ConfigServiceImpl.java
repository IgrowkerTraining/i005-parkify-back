package com.igrowker.feature.parkify.features.config.service;

import com.igrowker.feature.parkify.features.config.config.InitialConfigProperties;
import com.igrowker.feature.parkify.features.config.dto.FeatureFlagsDto;
import com.igrowker.feature.parkify.features.config.dto.InitialConfigResponse;
import com.igrowker.feature.parkify.features.config.dto.ThemeColorsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final InitialConfigProperties initialConfigProperties;

    @Override
    public InitialConfigResponse getInitialConfigData() {
        final ThemeColorsDto themeColorsDto = new ThemeColorsDto(
                initialConfigProperties.getThemeColors().getPrimary(),
                initialConfigProperties.getThemeColors().getSecondary()
        );
        final FeatureFlagsDto featureFlagsDto = new FeatureFlagsDto(
                initialConfigProperties.getFeatureFlags().getRecommendationsEnabled(),
                initialConfigProperties.getFeatureFlags().getOnlineBookingEnabled()
        );
        return new InitialConfigResponse(themeColorsDto, featureFlagsDto);
    }
}
