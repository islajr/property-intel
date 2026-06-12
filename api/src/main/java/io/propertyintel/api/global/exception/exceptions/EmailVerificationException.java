package io.propertyintel.api.global.exception.exceptions;

public class EmailVerificationException extends RuntimeException {

    public enum Reason {GENERIC, EXPIRED, ALREADY_USED, TOO_MANY_REQUESTS}

    private final Reason reason;

    // Default constructor
    public EmailVerificationException(String message) {
        this(message, Reason.GENERIC);
    }

    // Main constructor
    public EmailVerificationException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
