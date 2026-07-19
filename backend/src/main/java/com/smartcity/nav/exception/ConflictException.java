package com.smartcity.nav.exception;

/**
 * Thrown for state conflicts - e.g. registering with an email that
 * already exists. Mapped to HTTP 409 by GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
