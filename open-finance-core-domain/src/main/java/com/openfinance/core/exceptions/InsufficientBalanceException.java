package com.openfinance.core.exceptions;

public class InsufficientBalanceException extends DomainException {

    private static final String ERROR_CODE = "INSUFFICIENT_BALANCE";
    public InsufficientBalanceException(String message) {
        super(message, ERROR_CODE);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}