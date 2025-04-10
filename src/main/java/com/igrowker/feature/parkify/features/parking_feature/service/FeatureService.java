package com.igrowker.feature.parkify.features.parking_feature.service;

import com.igrowker.feature.parkify.features.parking_feature.dto.FeatureDto;

import java.util.List;

public interface FeatureService {
    List<FeatureDto> findAllFeatures();

    FeatureDto findFeatureBySlugOrThrow(String featureSlug);
}
