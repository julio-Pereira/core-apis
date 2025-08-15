package com.openfinance.core.exceptions;

import com.openfinance.core.domain.transaction.TransactionId;

/**
 * Exception thrown when a transaction is not found
 */
public class TransactionNotFoundException extends DomainException {

    private static final String ERROR_CODE = "TRANSACTION_NOT_FOUND";

    public TransactionNotFoundException(TransactionId transactionId) {
        super("Transaction not found: " + transactionId.getValue(), ERROR_CODE);
    }

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found: " + transactionId, ERROR_CODE);
    }
}