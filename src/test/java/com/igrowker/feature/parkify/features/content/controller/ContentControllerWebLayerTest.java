package com.igrowker.feature.parkify.features.content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.SocialLinkDto;
import com.igrowker.feature.parkify.features.content.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import({SecurityConfig.class, JwtService.class})
class ContentControllerWebLayerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ContentService contentService;
    @MockBean
    private UserDetailsService userDetailsService;
    private FooterContentResponse expectedFooterResponse;

    @BeforeEach
    void setUp() {
        final List<SocialLinkDto> socialLinks = List.of(
                new SocialLinkDto("x-twitter", "https://mock.x.com"),
                new SocialLinkDto("linkedin", "https://mock.linkedin.com")
        );
        expectedFooterResponse = new FooterContentResponse(
                "/mock-about",
                "mailto:mock@example.com",
                socialLinks
        );

        when(contentService.getFooterData()).thenReturn(expectedFooterResponse);
    }

    @Test
    void getFooterContent_shouldReturnFooterData_whenCalled() throws Exception {
        mockMvc.perform(get("/api/v1/content/footer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.aboutUsLink",
                        is(expectedFooterResponse.aboutUsLink())))
                .andExpect(jsonPath("$.contactLink",
                        is(expectedFooterResponse.contactLink())))
                .andExpect(jsonPath("$.socialLinks",
                        hasSize(2)))
                .andExpect(jsonPath("$.socialLinks[0].platform",
                        is("x-twitter")))
                .andExpect(jsonPath("$.socialLinks[0].url",
                        is("https://mock.x.com")))
                .andExpect(jsonPath("$.socialLinks[1].platform",
                        is("linkedin")))
                .andExpect(jsonPath("$.socialLinks[1].url",
                        is("https://mock.linkedin.com")));
    }

}