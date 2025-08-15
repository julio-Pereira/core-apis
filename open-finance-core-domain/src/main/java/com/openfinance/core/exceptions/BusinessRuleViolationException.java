package com.openfinance.core.exceptions;

/**
 * Exception thrown when a business rule is violated
 */
public class BusinessRuleViolationException extends NoStacktraceException {

  protected BusinessRuleViolationException(final String message, final Throwable t) {
    super(message, t);
  }

  public static BusinessRuleViolationException with(final String message, final Throwable t) {
    return new BusinessRuleViolationException(message, t);
  }

  public static BusinessRuleViolationException with(final String message) {
    return new BusinessRuleViolationException(message, null);
  }
}
