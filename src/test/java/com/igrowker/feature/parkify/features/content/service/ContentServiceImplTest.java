package com.igrowker.feature.parkify.features.content.service;

import com.igrowker.feature.parkify.features.content.config.FooterProperties;
import com.igrowker.feature.parkify.features.content.config.HomeProperties;
import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.HomeContentResponse;
import com.igrowker.feature.parkify.features.content.dto.SocialLinkDto;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ContentServiceImpl Unit Tests")
class ContentServiceImplTest {

    private static final String TEST_ABOUT_LINK = "/test-about";
    private static final String TEST_CONTACT_LINK = "mailto:test@example.com";
    private static final String TWITTER_PLATFORM = "x-twitter";
    private static final String TWITTER_URL = "https://test.x.com";
    private static final String INSTA_PLATFORM = "instagram";
    private static final String INSTA_URL = "https://test.instagram.com";

    private FooterProperties testFooterProperties;
    private HomeProperties testHomeProperties;
    private ContentServiceImpl contentServiceImpl;

    @BeforeEach
    void setUp() {
        testFooterProperties = getTestFooterProperties();
        testHomeProperties = new HomeProperties();

        final HomeProperties.ContentSectionProperties whoAreWeProps = new HomeProperties.ContentSectionProperties();
        whoAreWeProps.setTitle("Test Title Who");
        whoAreWeProps.setText("Test Text Who");
        testHomeProperties.setWhoAreWe(whoAreWeProps);

        final HomeProperties.ContentSectionProperties whatOfferProps = getContentSectionProperties();
        testHomeProperties.setWhatWeOffer(whatOfferProps);

        contentServiceImpl = new ContentServiceImpl(testFooterProperties, testHomeProperties);
    }

    private static HomeProperties.@NotNull ContentSectionProperties getContentSectionProperties() {
        final HomeProperties.ContentSectionProperties whatOfferProps = new HomeProperties.ContentSectionProperties();
        whatOfferProps.setTitle("Test Title Offer");
        final HomeProperties.ContentItemProperties item1 = new HomeProperties.ContentItemProperties();
        item1.setIcon("icon1");
        item1.setText("Text 1");
        final HomeProperties.ContentItemProperties item2 = new HomeProperties.ContentItemProperties();
        item2.setIcon("icon2");
        item2.setText("Text 2");
        whatOfferProps.setItems(List.of(item1, item2));
        return whatOfferProps;
    }

    private @NotNull FooterProperties getTestFooterProperties() {
        final FooterProperties props = new FooterProperties();
        final FooterProperties.SocialLinkProperties twitterProps = new FooterProperties.SocialLinkProperties();
        final FooterProperties.SocialLinkProperties instaProps = new FooterProperties.SocialLinkProperties();

        props.setAboutUsLink(TEST_ABOUT_LINK);
        props.setContactLink(TEST_CONTACT_LINK);
        twitterProps.setUrl(TWITTER_URL);
        instaProps.setUrl(INSTA_URL);
        props.setSocial(Map.of(
                TWITTER_PLATFORM, twitterProps,
                INSTA_PLATFORM, instaProps
        ));
        return props;
    }


    @Nested
    @DisplayName("When getFooterData is called")
    class GetFooterDataTests {

        @Test
        @DisplayName("Should map properties to DTO correctly when social links exist")
        void shouldMapPropertiesToDtoCorrectly_whenSocialLinksExist() {
            final FooterContentResponse actualResponse = contentServiceImpl.getFooterData();

            assertAll(
                    () -> assertThat(actualResponse.aboutUsLink()).isEqualTo(TEST_ABOUT_LINK),
                    () -> assertThat(actualResponse.contactLink()).isEqualTo(TEST_CONTACT_LINK),
                    () -> assertThat(actualResponse.socialLinks())
                            .isNotNull()
                            .hasSize(2)
                            .containsExactlyInAnyOrder(
                                    new SocialLinkDto(TWITTER_PLATFORM, TWITTER_URL),
                                    new SocialLinkDto(INSTA_PLATFORM, INSTA_URL)
                            )
            );
        }


        @Test
        @DisplayName("Should return empty social links list when social map is empty")
        void shouldReturnEmptySocialLinksList_whenSocialMapIsEmpty() {
            final FooterProperties emptySocialProperties = new FooterProperties();
            emptySocialProperties.setAboutUsLink(TEST_ABOUT_LINK);
            emptySocialProperties.setContactLink(TEST_CONTACT_LINK);
            emptySocialProperties.setSocial(Collections.emptyMap());

            final ContentServiceImpl serviceWithEmptySocial = new ContentServiceImpl(emptySocialProperties, testHomeProperties);
            final FooterContentResponse actualResponse = serviceWithEmptySocial.getFooterData();

            assertAll(
                    () -> assertThat(actualResponse.aboutUsLink()).isEqualTo(TEST_ABOUT_LINK),
                    () -> assertThat(actualResponse.contactLink()).isEqualTo(TEST_CONTACT_LINK),
                    () -> assertThat(actualResponse.socialLinks())
                            .isNotNull()
                            .isEmpty()
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when social map is null")
        void shouldThrowException_whenSocialMapIsNull() {
            final FooterProperties nullSocialProperties = new FooterProperties();
            nullSocialProperties.setAboutUsLink(TEST_ABOUT_LINK);
            nullSocialProperties.setContactLink(TEST_CONTACT_LINK);
            nullSocialProperties.setSocial(null); // Устанавливаем null

            final ContentServiceImpl serviceWithNullSocial = new ContentServiceImpl(nullSocialProperties, testHomeProperties);

            assertThrows(NullPointerException.class, serviceWithNullSocial::getFooterData,
                    "Expected NullPointerException when social map is null and accessed"
            );
        }
    }

    @Nested
    @DisplayName("When getHomeData is called")
    class GetHomeDataTests {

        @Test
        @DisplayName("Should map properties to DTO correctly")
        void shouldMapPropertiesToDtoCorrectly() {
            final HomeContentResponse actualResponse = contentServiceImpl.getHomeData();

            assertAll(
                    () -> assertThat(actualResponse.whoAreWe()).isNotNull(),
                    () -> assertThat(actualResponse.whoAreWe().title()).isEqualTo("Test Title Who"),
                    () -> assertThat(actualResponse.whoAreWe().text()).isEqualTo("Test Text Who"),
                    () -> assertThat(actualResponse.whoAreWe().items()).isNullOrEmpty(),

                    () -> assertThat(actualResponse.whatWeOffer()).isNotNull(),
                    () -> assertThat(actualResponse.whatWeOffer().title()).isEqualTo("Test Title Offer"),
                    () -> assertThat(actualResponse.whatWeOffer().text()).isNullOrEmpty(),
                    () -> assertThat(actualResponse.whatWeOffer().items()).hasSize(2),
                    () -> assertThat(actualResponse.whatWeOffer().items().get(0).icon()).isEqualTo("icon1"),
                    () -> assertThat(actualResponse.whatWeOffer().items().get(0).text()).isEqualTo("Text 1"),
                    () -> assertThat(actualResponse.whatWeOffer().items().get(1).icon()).isEqualTo("icon2"),
                    () -> assertThat(actualResponse.whatWeOffer().items().get(1).text()).isEqualTo("Text 2")
            );
        }

        @Test
        @DisplayName("Should handle empty items list correctly")
        void shouldHandleEmptyItemsListCorrectly() {
            final HomeProperties specificHomeProperties = new HomeProperties();
            final HomeProperties.ContentSectionProperties emptyItemsSection = new HomeProperties.ContentSectionProperties();
            emptyItemsSection.setTitle("Empty Items Title");
            emptyItemsSection.setItems(Collections.emptyList());
            specificHomeProperties.setWhatWeOffer(emptyItemsSection);
            specificHomeProperties.setWhoAreWe(testHomeProperties.getWhoAreWe());

            final ContentServiceImpl specificService = new ContentServiceImpl(testFooterProperties, specificHomeProperties);
            final HomeContentResponse actualResponse = specificService.getHomeData();

            assertAll(
                    () -> assertThat(actualResponse.whatWeOffer()).isNotNull(),
                    () -> assertThat(actualResponse.whatWeOffer().title()).isEqualTo("Empty Items Title"),
                    () -> assertThat(actualResponse.whatWeOffer().items()).isNotNull().isEmpty()
            );
        }
    }
}