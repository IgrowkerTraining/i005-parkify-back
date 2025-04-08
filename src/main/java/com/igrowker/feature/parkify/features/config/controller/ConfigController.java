package com.igrowker.feature.parkify.features.config.controller;

import com.igrowker.feature.parkify.features.config.dto.InitialConfigResponse;
import com.igrowker.feature.parkify.features.config.service.ConfigService; // <-- Изменили импорт
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/initial")
    public ResponseEntity<InitialConfigResponse> getInitialConfig() {
        final InitialConfigResponse configData = configService.getInitialConfigData();
        return ResponseEntity.ok(configData);
    }
}
