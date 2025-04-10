package com.igrowker.feature.parkify.features.parking_feature.controller;

import com.igrowker.feature.parkify.features.parking_feature.dto.FeatureDto;
import com.igrowker.feature.parkify.features.parking_feature.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    public ResponseEntity<List<FeatureDto>> getAllFeatures() {
        return ResponseEntity.ok(featureService.findAllFeatures());
    }

    @GetMapping("/{featureSlug}")
    public ResponseEntity<FeatureDto> getFeatureBySlug(@PathVariable String featureSlug) {
        return ResponseEntity.ok(featureService.findFeatureBySlugOrThrow(featureSlug));
    }

}