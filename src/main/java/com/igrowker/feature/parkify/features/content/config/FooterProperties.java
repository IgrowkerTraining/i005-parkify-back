package com.igrowker.feature.parkify.features.content.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "parkify.content.footer")
@Data
@Validated
public class FooterProperties {

    @NotBlank(message = "Footer 'about us' link cannot be blank")
    private String aboutUsLink;

    @NotBlank(message = "Footer 'contact' link cannot be blank")
    private String contactLink;

    @NotNull(message = "Social links map cannot be null")
    private Map<String, SocialLinkProperties> social = new HashMap<>();

    @Data
    @Validated
    public static class SocialLinkProperties {
        @NotBlank(message = "Social link URL cannot be blank")
        private String url;
    }
}
