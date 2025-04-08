package com.igrowker.feature.parkify.features.content.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "parkify.content.home")
@Data
@Validated
public class HomeProperties {

    @Valid
    private ContentSectionProperties whoAreWe = new ContentSectionProperties();
    @Valid
    private ContentSectionProperties whatWeOffer = new ContentSectionProperties();


    @Data
    @Validated
    public static class ContentSectionProperties {
        @NotBlank(message = "Section title cannot be blank")
        private String title;
        private String text;

        @Valid
        private List<ContentItemProperties> items = new ArrayList<>();
    }

    @Data
    @Validated
    public static class ContentItemProperties {
        private String icon;
        @NotBlank(message = "Item text cannot be blank")
        private String text;
    }
}