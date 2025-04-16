package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import com.igrowker.feature.parkify.features.user.dto.response.UserDetailsResponse;
import com.igrowker.feature.parkify.features.user.model.User;
import com.igrowker.feature.parkify.features.user.repository.UserRepository;
import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import com.igrowker.feature.parkify.features.booking.mapper.BookingMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, BookingMapper bookingMapper) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailsResponse getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<BookingResponse> bookingResponses = user.getBookings().stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());

        return new UserDetailsResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                bookingResponses
        );
    }

    @Override
    public void updateUserLocation(String name, LocationUpdateRequest request) {
        // Implementación pendiente
    }

    @Override
    public PublicUserResponse getPublicUserById(String userId) {
        return null; // Implementación pendiente
    }
}
