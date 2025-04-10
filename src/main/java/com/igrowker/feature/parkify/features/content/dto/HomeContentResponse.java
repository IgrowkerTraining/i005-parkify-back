package com.igrowker.feature.parkify.features.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeContentResponse {
    private ContentSectionDto whoAreWe;
    private ContentSectionDto whatWeOffer;
}
