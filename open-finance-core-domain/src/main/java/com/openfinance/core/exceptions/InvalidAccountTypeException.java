package com.openfinance.core.exceptions;

import com.openfinance.core.domain.account.AccountType;

/**
 * Exception thrown when an invalid account type is used for a specific operation
 */
public class InvalidAccountTypeException extends DomainException {

    private static final String ERROR_CODE = "INVALID_ACCOUNT_TYPE";

    public InvalidAccountTypeException(AccountType accountType, String operation) {
        super("Account type " + accountType + " is not valid for operation: " + operation, ERROR_CODE);
    }

    public InvalidAccountTypeException(String message) {
        super(message, ERROR_CODE);
    }
}
