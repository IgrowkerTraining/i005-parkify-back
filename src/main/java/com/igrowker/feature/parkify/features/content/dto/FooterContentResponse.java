package com.igrowker.feature.parkify.features.content.dto;

import java.util.List;

public record FooterContentResponse(
        String aboutUsLink,
        String contactLink,
        List<SocialLinkDto> socialLinks) {
}
