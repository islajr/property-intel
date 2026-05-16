package io.propertyintel.api.global.exception.exceptions;

public class ForbiddenException extends RuntimeException {

    // Default constructor
    public ForbiddenException() {
        super("Forbidden");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
