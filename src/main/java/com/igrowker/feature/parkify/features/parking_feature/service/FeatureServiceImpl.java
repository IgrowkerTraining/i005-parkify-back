package com.igrowker.feature.parkify.features.parking_feature.service;

import com.igrowker.feature.parkify.features.parking_feature.dto.FeatureDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureServiceImpl implements FeatureService {
    @Override
    public List<FeatureDto> findAllFeatures() {
        return List.of();
    }

    @Override
    public FeatureDto findFeatureBySlugOrThrow(String featureSlug) {
        return null;
    }
}
