package com.openfinance.core.exceptions;

import com.openfinance.core.domain.transaction.CompletedAuthorisedPaymentIndicator;
import com.openfinance.core.domain.transaction.TransactionId;

/**
 * Exception thrown when a transaction is in an invalid state for a specific operation
 */
public class InvalidTransactionStateException extends DomainException {

  private static final String ERROR_CODE = "INVALID_TRANSACTION_STATE";

  public InvalidTransactionStateException(TransactionId transactionId,
                                          CompletedAuthorisedPaymentIndicator currentState,
                                          String operation) {
    super("Transaction " + transactionId.getValue() + " is in state " + currentState +
            " and cannot perform operation: " + operation, ERROR_CODE);
  }

  public InvalidTransactionStateException(String message) {
    super(message, ERROR_CODE);
  }
}