package com.igrowker.feature.parkify.features.operation.service;

import com.igrowker.feature.parkify.features.operation.dto.response.OperationStatusResponse;

public interface OperationStatusService {
    OperationStatusResponse getOperationStatus(String operationId);
}
