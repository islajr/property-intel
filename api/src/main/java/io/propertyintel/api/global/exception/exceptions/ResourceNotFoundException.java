package io.propertyintel.api.global.exception.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    // Default constructor
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
