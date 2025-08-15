package com.openfinance.core.exceptions;

public class ValidationException extends NoStacktraceException {

    protected ValidationException(final String message, final Throwable t) {
        super(message, t);
    }

    public static ValidationException with(final String message, final Throwable t) {
        return new ValidationException(message, t);
    }

    public static ValidationException with(final String message) {
        return new ValidationException(message, null);
    }
}
