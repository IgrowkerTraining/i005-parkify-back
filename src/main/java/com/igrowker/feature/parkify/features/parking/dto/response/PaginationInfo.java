package com.igrowker.feature.parkify.features.parking.dto.response;

public record PaginationInfo(
        long total,
        int limit,
        int offset
) {
}