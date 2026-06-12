package io.propertyintel.api.global.exception.exceptions;

public class RateLimitException extends RuntimeException {

    // Default constructor
    public RateLimitException() {
        super("Too many requests");
    }

    public RateLimitException(String message) {
        super(message);
    }
}
