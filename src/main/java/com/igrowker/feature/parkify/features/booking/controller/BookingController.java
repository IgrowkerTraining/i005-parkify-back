package com.igrowker.feature.parkify.features.booking.controller;

import com.igrowker.feature.parkify.features.booking.dto.request.BookingRequest;
import com.igrowker.feature.parkify.features.booking.dto.request.UpdateBookingStatusRequest;
import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import com.igrowker.feature.parkify.features.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // #24
    @PostMapping
    public ResponseEntity<BookingResponse> requestBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        final String driverEmail = authentication.getName();
        final BookingResponse createdBooking = bookingService.createBookingRequest(
                driverEmail, request
        );
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdBooking.getBookingRequestId()).toUri();
        return ResponseEntity.created(location).body(createdBooking);
    }

    // #30
    @PatchMapping("/{bookingRequestId}")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable String bookingRequestId,
            @Valid @RequestBody UpdateBookingStatusRequest request,
            Authentication authentication) {
        final String userEmail = authentication.getName();
        final BookingResponse updatedBooking = bookingService.updateBookingStatus(
                userEmail, bookingRequestId, request.status()
        );
        return ResponseEntity.ok(updatedBooking);
    }
}
