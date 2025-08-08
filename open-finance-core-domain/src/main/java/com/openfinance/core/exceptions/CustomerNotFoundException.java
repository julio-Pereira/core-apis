package com.openfinance.core.exceptions;

public class CustomerNotFoundException extends DomainException {

    private static final String ERROR_CODE = "CUSTOMER_NOT_FOUND";
    public CustomerNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
    
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE ,cause);
    }
}