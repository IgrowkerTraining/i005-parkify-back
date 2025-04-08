package com.igrowker.feature.parkify.features.content.service;

import com.igrowker.feature.parkify.features.content.dto.FooterContentResponse;
import com.igrowker.feature.parkify.features.content.dto.HomeContentResponse;

public interface ContentService {
    FooterContentResponse getFooterData();
    HomeContentResponse getHomeData();
}
