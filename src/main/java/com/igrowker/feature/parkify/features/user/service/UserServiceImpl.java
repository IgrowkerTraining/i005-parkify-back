package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public void updateUserLocation(String name, LocationUpdateRequest request) {

    }

    @Override
    public PublicUserResponse getPublicUserById(String userId) {
        return null;
    }
}
