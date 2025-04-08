package com.igrowker.feature.parkify.features.content.service;

import com.igrowker.feature.parkify.features.content.config.FooterProperties;
import com.igrowker.feature.parkify.features.content.config.HomeProperties;
import com.igrowker.feature.parkify.features.content.dto.ContentItemDto;
import com.igrowker.feature.parkify.features.content.dto.ContentSectionDto;
import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.HomeContentResponse;
import com.igrowker.feature.parkify.features.content.dto.SocialLinkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final FooterProperties footerProperties;
    private final HomeProperties homeProperties;

    @Override
    public FooterContentResponse getFooterData() {
        final List<SocialLinkDto> socialLinks = footerProperties.getSocial().entrySet().stream()
                .map(entry -> new SocialLinkDto(
                        entry.getKey(), entry.getValue().getUrl())
                )
                .toList();

        return new FooterContentResponse(
                footerProperties.getAboutUsLink(),
                footerProperties.getContactLink(),
                socialLinks
        );
    }

    @Override
    public HomeContentResponse getHomeData() {
        final ContentSectionDto whoAreWeDto = new ContentSectionDto(
                homeProperties.getWhoAreWe().getTitle(),
                homeProperties.getWhoAreWe().getText(),
                null
        );

        final List<ContentItemDto> whatWeOfferItems = homeProperties.getWhatWeOffer().getItems()
                .stream()
                .map(itemProp -> new ContentItemDto(
                        itemProp.getIcon(), itemProp.getText())
                )
                .toList();
        final ContentSectionDto whatWeOfferDto = new ContentSectionDto(
                homeProperties.getWhatWeOffer().getTitle(),
                null,
                whatWeOfferItems
        );
        return new HomeContentResponse(whoAreWeDto, whatWeOfferDto);
    }
}
