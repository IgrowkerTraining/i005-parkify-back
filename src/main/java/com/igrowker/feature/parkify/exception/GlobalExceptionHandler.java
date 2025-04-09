package com.igrowker.feature.parkify.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        final Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (existingValue, newValue) -> existingValue + "; " + newValue
                ));
        log.warn("Validation failed for request [{}]: {}", request.getRequestURI(), errors);
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request
    ) {
        log.warn("Authentication failed for request [{}]: {}",
                request.getRequestURI(), ex.getMessage()
        );
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication Failed: " + ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request
    ) {
        log.warn("Access denied for request [{}]: {}", request.getRequestURI(), ex.getMessage());
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Access Denied: You do not have permission to access this resource.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ParkingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParkingNotFoundException(
            ParkingNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Parking lookup failed for request [{}]: {}",
                request.getRequestURI(), ex.getMessage()
        );
        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OwnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOwnerNotFoundException(
            OwnerNotFoundException ex, HttpServletRequest request) {
        log.error("Data integrity issue detected for request [{}]: {}",
                request.getRequestURI(), ex.getMessage()
        );
        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request
    ) {
        log.error("Unexpected error occurred for request [{}]: {}",
                request.getRequestURI(), ex.getMessage(), ex
        );
        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> details
    ) {
        public ErrorResponse(
                Instant timestamp, int status, String error, String message, String path
        ) {
            this(timestamp, status, error, message, path, null);
        }
    }
}
