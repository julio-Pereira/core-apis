package com.openfinance.core.exceptions;

/**
 * Exception thrown when an invalid amount is provided
 */
public class InvalidAmountException extends DomainException {

  private static final String ERROR_CODE = "INVALID_AMOUNT";

  public InvalidAmountException(String message) {
    super(message, ERROR_CODE);
  }

  public InvalidAmountException(String message, Throwable cause) {
    super(message, ERROR_CODE, cause);
  }
}
