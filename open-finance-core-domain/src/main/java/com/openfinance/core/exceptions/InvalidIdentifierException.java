package com.openfinance.core.exceptions;

/**
 * Exception thrown when an invalid identifier format is provided
 */
public class InvalidIdentifierException extends DomainException {

    private static final String ERROR_CODE = "INVALID_IDENTIFIER";

    public InvalidIdentifierException(String identifierType, String value) {
        super("Invalid " + identifierType + " format: " + value, ERROR_CODE);
    }

    public InvalidIdentifierException(String message) {
        super(message, ERROR_CODE);
    }
}
