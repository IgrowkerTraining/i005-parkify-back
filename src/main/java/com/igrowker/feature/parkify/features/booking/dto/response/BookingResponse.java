package com.igrowker.feature.parkify.features.booking.dto.response;

import com.igrowker.feature.parkify.common.dto.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String bookingRequestId;
    private String userId;
    private String parkingId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime bookingTime;
    private OperationStatus status;

    public BookingResponse(Long id, Object startDate, Object endDate) {
    }

    public <__TMP__> __TMP__ getId() {
    return (__TMP__) bookingRequestId;}
}
