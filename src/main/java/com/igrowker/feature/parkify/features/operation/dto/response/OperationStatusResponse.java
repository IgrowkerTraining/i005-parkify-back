package com.igrowker.feature.parkify.features.operation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.igrowker.feature.parkify.common.dto.OperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationStatusResponse {
    private String operationId;
    private OperationStatus status;
    private Integer progressPercent;
    private String resultUrl;
    private String errorMessage;
}
