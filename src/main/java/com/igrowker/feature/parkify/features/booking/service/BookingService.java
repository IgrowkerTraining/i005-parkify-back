package com.igrowker.feature.parkify.features.booking.service;

import com.igrowker.feature.parkify.features.booking.dto.request.BookingRequest;
import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface BookingService {
    BookingResponse createBookingRequest(String driverEmail, @Valid BookingRequest request);

    BookingResponse updateBookingStatus(
            String userEmail, String bookingRequestId,
            @NotBlank(message = "Status cannot be blank") String status
    );
}
