package com.openfinance.core.exceptions;

public class InsufficientBalanceException extends DomainException {
    
    public InsufficientBalanceException(String message) {
        super(message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}