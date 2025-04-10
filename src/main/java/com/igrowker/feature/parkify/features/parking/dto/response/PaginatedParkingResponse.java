package com.igrowker.feature.parkify.features.parking.dto.response;

import java.util.List;

public record PaginatedParkingResponse(
        List<ParkingSummaryResponse> data,
        PaginationInfo pagination
) {
}
