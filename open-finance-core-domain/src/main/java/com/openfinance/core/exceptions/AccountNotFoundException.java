package com.openfinance.core.exceptions;

public class AccountNotFoundException extends DomainException {

    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    public AccountNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public AccountNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}