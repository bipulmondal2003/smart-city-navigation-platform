package com.smartcity.nav.exception;

/**
 * Thrown for malformed/invalid business input that isn't caught by
 * bean validation - e.g. an invalid refresh token. Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
