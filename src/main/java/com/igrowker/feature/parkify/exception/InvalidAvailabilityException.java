package com.igrowker.feature.parkify.exception;

public class InvalidAvailabilityException extends IllegalArgumentException {
    public InvalidAvailabilityException(String message) {
        super(message);
    }
}
