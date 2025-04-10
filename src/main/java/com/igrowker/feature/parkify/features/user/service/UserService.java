package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import jakarta.validation.Valid;

public interface UserService {
    void updateUserLocation(String name, @Valid LocationUpdateRequest request);

    PublicUserResponse getPublicUserById(String userId);
}
