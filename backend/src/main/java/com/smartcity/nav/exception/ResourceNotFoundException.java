package com.smartcity.nav.exception;

/**
 * Thrown when a requested entity (User, Location, Road, ...) does not exist.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
