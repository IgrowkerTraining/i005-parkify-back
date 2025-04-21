package com.igrowker.feature.parkify.features.config.controller;

import com.igrowker.feature.parkify.features.config.dto.InitialConfigResponse;
import com.igrowker.feature.parkify.features.config.service.ConfigService; // <-- Изменили импорт
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Configuration", description = "Endpoints for application configuration settings")
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    // #10
    @Operation(
            summary = "Get Initial Application Configuration (#10)",
            description = "Returns initial configuration data like theme colors and feature flags."
    )
    @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = InitialConfigResponse.class)))
    @GetMapping("/initial")
    public ResponseEntity<InitialConfigResponse> getInitialConfig() {
        final InitialConfigResponse configData = configService.getInitialConfigData();
        return ResponseEntity.ok(configData);
    }
}
