package com.igrowker.feature.parkify.features.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentSectionDto {
    private String title;
    private String text;
    private List<ContentItemDto> items;
}
