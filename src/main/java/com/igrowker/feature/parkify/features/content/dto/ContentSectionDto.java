package com.igrowker.feature.parkify.features.content.dto;

import java.util.List;

public record ContentSectionDto(
        String title,
        String text,
        List<ContentItemDto> items) {
}
