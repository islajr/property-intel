package io.propertyintel.api.global.exception.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UnauthorizedException extends AuthenticationException {

    // Default constructor
    public UnauthorizedException() {
        super("Unauthorized");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
