package io.propertyintel.api.global.exception.exceptions;

public class BadRequestException extends RuntimeException {

    // Default constructor
    public BadRequestException() {
        super("Bad Request");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
