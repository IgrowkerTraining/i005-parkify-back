package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AuthUserRepository authUserRepository;
    @Override
    public void updateUserLocation(String email, LocationUpdateRequest request) {
        log.info("Updating location for user: {}", email);
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setLatitude(request.latitude());
        user.setLongitude(request.longitude());
        user.setLocationUpdatedAt(LocalDateTime.now());

        authUserRepository.save(user);
    }

    @Override
    public PublicUserResponse getPublicUserById(String userId) {
        return null;
    }
}
