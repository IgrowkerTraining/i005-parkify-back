package com.igrowker.feature.parkify.features.booking.mapper;

import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import com.igrowker.feature.parkify.features.booking.entities.Booking;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate()
        );
    }
}
