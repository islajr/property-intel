package io.propertyintel.api.global.exception.exceptions;

public class UnauthorizedException extends RuntimeException {

    // Default constructor
    public UnauthorizedException() {
        super("Unauthorized");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
