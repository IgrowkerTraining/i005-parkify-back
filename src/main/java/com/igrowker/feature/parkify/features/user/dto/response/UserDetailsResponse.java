package com.igrowker.feature.parkify.features.user.dto.response;

import com.igrowker.feature.parkify.features.booking.dto.response.BookingResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class UserDetailsResponse {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<BookingResponse> bookings;

    public UserDetailsResponse(String firstName, String lastName, String email, List<BookingResponse> bookings) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.bookings = bookings;
    }

}
