package com.igrowker.feature.parkify.exception;

public class OwnerNotFoundException extends RuntimeException {
    public OwnerNotFoundException(String message) {
        super(message);
    }

    public OwnerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
