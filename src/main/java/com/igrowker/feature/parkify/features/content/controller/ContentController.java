package com.igrowker.feature.parkify.features.content.controller;

import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.HomeContentResponse;
import com.igrowker.feature.parkify.features.content.service.ContentService;
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

@Tag(name = "Content", description = "Endpoints for retrieving static content")
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // #13
    @Operation(
            summary = "Get Footer Content (#13)",
            description = "Returns content data required for the application footer (links, social media URLs)."
    )
    @ApiResponse(responseCode = "200", description = "Footer content retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FooterContentResponse.class)))
    @GetMapping("/footer")
    public ResponseEntity<FooterContentResponse> getFooterContent() {
        final FooterContentResponse footerData = contentService.getFooterData();
        return ResponseEntity.ok(footerData);
    }

    // #11
    @Operation(
            summary = "Get Home Screen Content (#11)",
            description = "Returns content data for the main home screen sections (e.g., 'Who are we', 'What we offer')."
    )
    @ApiResponse(responseCode = "200", description = "Home screen content retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HomeContentResponse.class)))
    @GetMapping("/home")
    public ResponseEntity<HomeContentResponse> getHomeContent() {
        final HomeContentResponse homeData = contentService.getHomeData();
        return ResponseEntity.ok(homeData);
    }

}
