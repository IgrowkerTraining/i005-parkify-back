package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import com.igrowker.feature.parkify.features.booking.entities.Booking;
import com.igrowker.feature.parkify.features.booking.mapper.BookingMapper;
import com.igrowker.feature.parkify.features.user.dto.response.UserDetailsResponse;
import com.igrowker.feature.parkify.features.user.model.User;
import com.igrowker.feature.parkify.features.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private BookingMapper bookingMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        bookingMapper = mock(BookingMapper.class);
        userService = new UserServiceImpl(userRepository, bookingMapper);
    }

    @Test
    void getCurrentUserDetails_returnsUserDetailsResponse() {
        // Mock email del usuario autenticado
        String email = "test@example.com";

        // Mock reservas
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setStartDate(LocalDateTime.now().minusDays(1));
        booking.setEndDate(LocalDateTime.now());

        // Mock usuario
        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setBookings(Collections.singletonList(booking));

        // BookingResponse de ejemplo
        BookingResponse bookingResponse = new BookingResponse(
                booking.getId(), booking.getStartDate(), booking.getEndDate()
        );

        // Simulamos autenticación de Spring Security
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            // Simula que el usuario existe en BD
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            mocked.when((MockedStatic.Verification) BookingMapper.toResponse(booking)).thenReturn(bookingResponse);

            // Ejecutamos el método
            UserDetailsResponse response = userService.getCurrentUserDetails();

            // Validaciones
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
            assertEquals(email, response.getEmail());
            assertEquals(1, response.getBookings().size());
            assertEquals(Optional.ofNullable(bookingResponse.getId()), response.getBookings().get(0).getId());
        }
    }
}
