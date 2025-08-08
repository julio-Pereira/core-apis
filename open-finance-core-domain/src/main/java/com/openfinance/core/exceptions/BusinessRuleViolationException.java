package com.openfinance.core.exceptions;

/**
 * Exception thrown when a business rule is violated
 */
public class BusinessRuleViolationException extends DomainException {

  private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

  public BusinessRuleViolationException(String message) {
    super(message, ERROR_CODE);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, ERROR_CODE, cause);
  }
}
