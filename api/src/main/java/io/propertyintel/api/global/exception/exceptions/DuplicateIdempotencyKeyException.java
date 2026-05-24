package io.propertyintel.api.global.exception.exceptions;

public class DuplicateIdempotencyKeyException extends RuntimeException {

    // Default constructor
    public DuplicateIdempotencyKeyException() {
        super("Request already processing");
    }

    public DuplicateIdempotencyKeyException(String message) {
        super(message);
    }
}
