package com.openfinance.usecase.exception;

import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.exceptions.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Exception handler for use case layer
 * Provides structured error handling and logging for business operations
 */
@Slf4j
@Component
public class UseCaseExceptionHandler {

    /**
     * Handles domain exceptions and converts them to use case exceptions
     */
    public UseCaseException handleDomainException(DomainException domainException, String operation) {
        log.error("Domain exception occurred during operation: {} - Error: {}, ",
                operation, domainException.getMessage());

        return UseCaseException.builder()
                .message(domainException.getMessage())
                .operation(operation)
                .timestamp(LocalDateTime.now())
                .cause(domainException)
                .build();
    }

    /**
     * Handles business rule violations
     */
    public UseCaseException handleBusinessRuleViolation(BusinessRuleViolationException exception, String operation) {
        log.warn("Business rule violation during operation: {} - Error: {}",
                operation, exception.getMessage(), exception);

        return UseCaseException.builder()
                .message(exception.getMessage())
                .operation(operation)
                .timestamp(LocalDateTime.now())
                .cause(exception)
                .build();
    }

    /**
     * Handles unexpected exceptions
     */
    public UseCaseException handleUnexpectedException(Exception exception, String operation) {
        log.error("Unexpected exception during operation: {} - Error: {}",
                operation, exception.getMessage(), exception);

        return UseCaseException.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred: " + exception.getMessage())
                .operation(operation)
                .timestamp(LocalDateTime.now())
                .cause(exception)
                .build();
    }

    /**
     * Handles rate limit exceeded scenarios
     */
    public UseCaseException handleRateLimitExceeded(String organizationId, String endpoint) {
        String message = String.format("Rate limit exceeded for organization %s on endpoint %s",
                organizationId, endpoint);

        log.warn("Rate limit exceeded - OrganizationId: {}, Endpoint: {}", organizationId, endpoint);

        return UseCaseException.builder()
                .errorCode("RATE_LIMIT_EXCEEDED")
                .message(message)
                .operation("RATE_LIMIT_VALIDATION")
                .timestamp(LocalDateTime.now())
                .httpStatusCode(429)
                .build();
    }

    /**
     * Handles consent validation failures
     */
    public UseCaseException handleConsentValidationFailure(String consentId, String reason) {
        String message = String.format("Consent validation failed for consent %s: %s", consentId, reason);

        log.warn("Consent validation failed - ConsentId: {}, Reason: {}", consentId, reason);

        return UseCaseException.builder()
                .errorCode("INVALID_CONSENT")
                .message(message)
                .operation("CONSENT_VALIDATION")
                .timestamp(LocalDateTime.now())
                .httpStatusCode(401)
                .build();
    }

    /**
     * Handles permission validation failures
     */
    public UseCaseException handlePermissionDenied(String consentId, String requiredPermission) {
        String message = String.format("Permission denied for consent %s. Required permission: %s",
                consentId, requiredPermission);

        log.warn("Permission denied - ConsentId: {}, RequiredPermission: {}", consentId, requiredPermission);

        return UseCaseException.builder()
                .errorCode("PERMISSION_DENIED")
                .message(message)
                .operation("PERMISSION_VALIDATION")
                .timestamp(LocalDateTime.now())
                .httpStatusCode(403)
                .build();
    }

    /**
     * Custom exception for use case layer
     */
    @lombok.Builder
    @lombok.Getter
    public static class UseCaseException extends RuntimeException {
        private final String errorCode;
        private final String message;
        private final String operation;
        private final LocalDateTime timestamp;
        private final Throwable cause;
        private final int httpStatusCode;

        public UseCaseException(String errorCode, String message, String operation,
                                LocalDateTime timestamp, Throwable cause, int httpStatusCode) {
            super(message, cause);
            this.errorCode = errorCode;
            this.message = message;
            this.operation = operation;
            this.timestamp = timestamp;
            this.cause = cause;
            this.httpStatusCode = httpStatusCode > 0 ? httpStatusCode : 500;
        }

        public boolean hasHttpStatusCode() {
            return httpStatusCode > 0;
        }
    }
}