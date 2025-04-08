package com.igrowker.feature.parkify.features.content.controller;

import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.HomeContentResponse;
import com.igrowker.feature.parkify.features.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/footer")
    public ResponseEntity<FooterContentResponse> getFooterContent() {
        final FooterContentResponse footerData = contentService.getFooterData();
        return ResponseEntity.ok(footerData);
    }

    @GetMapping("/home")
    public ResponseEntity<HomeContentResponse> getHomeContent() {
        final HomeContentResponse homeData = contentService.getHomeData();
        return ResponseEntity.ok(homeData);
    }

}
