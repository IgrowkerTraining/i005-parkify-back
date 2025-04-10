package com.igrowker.feature.parkify.features.operation.controller;

import com.igrowker.feature.parkify.features.operation.dto.response.OperationStatusResponse;
import com.igrowker.feature.parkify.features.operation.service.OperationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
public class OperationStatusController {

    private final OperationStatusService operationStatusService; // Нужен сервис

    // #32 Получение статуса операции
    @GetMapping("/{operationId}/status")
    public ResponseEntity<OperationStatusResponse> getOperationStatus(@PathVariable String operationId) {
        OperationStatusResponse status = operationStatusService.getOperationStatus(operationId);
        return ResponseEntity.ok(status);
    }
}
