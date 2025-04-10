package com.igrowker.feature.parkify.features.booking.service;

import com.igrowker.feature.parkify.features.booking.dto.request.BookingRequest;
import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl implements BookingService {
    @Override
    public BookingResponse createBookingRequest(String driverEmail, BookingRequest request) {
        return null;
    }

    @Override
    public BookingResponse updateBookingStatus(
            String userEmail, String bookingRequestId, String status
    ) {
        return null;
    }
}
