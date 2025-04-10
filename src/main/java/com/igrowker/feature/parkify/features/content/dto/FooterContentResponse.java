package com.igrowker.feature.parkify.features.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FooterContentResponse {
    private String aboutUsLink;
    private String contactLink;
    private List<SocialLinkDto> socialLinks;
}
