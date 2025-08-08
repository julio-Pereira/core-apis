package com.openfinance.core.exceptions;

import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.Amount;

/**
 * Exception thrown when an account has insufficient funds for an operation
 */
public class InsufficientFundsException extends DomainException {

    private static final String ERROR_CODE = "INSUFFICIENT_FUNDS";

    public InsufficientFundsException(AccountId accountId, Amount requestedAmount, Amount availableAmount) {
        super("Insufficient funds in account " + accountId.getValue() +
                ". Requested: " + requestedAmount + ", Available: " + availableAmount, ERROR_CODE);
    }

    public InsufficientFundsException(String message) {
        super(message, ERROR_CODE);
    }
}