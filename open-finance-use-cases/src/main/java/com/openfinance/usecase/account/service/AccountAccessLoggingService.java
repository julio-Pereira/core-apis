package com.openfinance.usecase.account.service;

import com.openfinance.usecase.account.input.GetAccountsInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for structured logging of account access operations
 * Provides audit trail and monitoring capabilities for Open Finance compliance
 */
@Slf4j
@Service
public class AccountAccessLoggingService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Logs the start of account access operation
     */
    public void logAccountAccessStart(GetAccountsInput input) {
        log.info("ACCOUNT_ACCESS_START - ConsentId: {}, OrganizationId: {}, Operation: GET_ACCOUNTS, " +
                        "Page: {}, PageSize: {}, AccountTypeFilter: {}, InteractionId: {}, Timestamp: {}",
                input.consentId(),
                input.organizationId(),
                input.page(),
                input.pageSize(),
                input.accountType().map(Enum::name).orElse("NONE"),
                input.xFapiInteractionId(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Logs successful completion of account access operation
     */
    public void logAccountAccessSuccess(GetAccountsInput input, int accountCount, long executionTimeMs) {
        log.info("ACCOUNT_ACCESS_SUCCESS - ConsentId: {}, OrganizationId: {}, Operation: GET_ACCOUNTS, " +
                        "AccountsReturned: {}, ExecutionTimeMs: {}, InteractionId: {}, Timestamp: {}",
                input.consentId(),
                input.organizationId(),
                accountCount,
                executionTimeMs,
                input.xFapiInteractionId(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Logs failed account access operation
     */
    public void logAccountAccessFailure(GetAccountsInput input, String errorCode, String errorMessage, long executionTimeMs) {
        log.error("ACCOUNT_ACCESS_FAILURE - ConsentId: {}, OrganizationId: {}, Operation: GET_ACCOUNTS, " +
                        "ErrorCode: {}, ErrorMessage: {}, ExecutionTimeMs: {}, InteractionId: {}, Timestamp: {}",
                input.consentId(),
                input.organizationId(),
                errorCode,
                errorMessage,
                executionTimeMs,
                input.xFapiInteractionId(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Logs rate limit validation
     */
    public void logRateLimitValidation(String organizationId, boolean withinLimits, long remainingRequests) {
        if (withinLimits) {
            log.debug("RATE_LIMIT_CHECK - OrganizationId: {}, Status: WITHIN_LIMITS, RemainingRequests: {}, Timestamp: {}",
                    organizationId,
                    remainingRequests,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            log.warn("RATE_LIMIT_EXCEEDED - OrganizationId: {}, Status: EXCEEDED, Timestamp: {}",
                    organizationId,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    /**
     * Logs consent validation
     */
    public void logConsentValidation(String consentId, boolean isValid, boolean hasPermission) {
        log.debug("CONSENT_VALIDATION - ConsentId: {}, IsValid: {}, HasAccountsReadPermission: {}, Timestamp: {}",
                consentId,
                isValid,
                hasPermission,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Logs pagination key usage
     */
    public void logPaginationKeyUsage(String paginationKey, boolean isValid, boolean isExpired) {
        if (paginationKey != null && !paginationKey.trim().isEmpty()) {
            log.debug("PAGINATION_KEY_USAGE - PaginationKeyPresent: true, IsValid: {}, IsExpired: {}, Timestamp: {}",
                    isValid,
                    isExpired,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            log.debug("PAGINATION_KEY_USAGE - PaginationKeyPresent: false, Timestamp: {}",
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    /**
     * Logs external service call
     */
    public void logExternalServiceCall(String consentId, String operation, boolean success, long responseTimeMs) {
        if (success) {
            log.debug("EXTERNAL_SERVICE_CALL - ConsentId: {}, Operation: {}, Status: SUCCESS, ResponseTimeMs: {}, Timestamp: {}",
                    consentId,
                    operation,
                    responseTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            log.error("EXTERNAL_SERVICE_CALL - ConsentId: {}, Operation: {}, Status: FAILURE, ResponseTimeMs: {}, Timestamp: {}",
                    consentId,
                    operation,
                    responseTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    /**
     * Logs performance metrics
     */
    public void logPerformanceMetrics(String operation, long totalExecutionTimeMs,
                                      long validationTimeMs, long externalCallTimeMs, long processingTimeMs) {
        log.info("PERFORMANCE_METRICS - Operation: {}, TotalTimeMs: {}, ValidationTimeMs: {}, " +
                        "ExternalCallTimeMs: {}, ProcessingTimeMs: {}, Timestamp: {}",
                operation,
                totalExecutionTimeMs,
                validationTimeMs,
                externalCallTimeMs,
                processingTimeMs,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Logs compliance metrics for Open Finance monitoring
     */
    public void logComplianceMetrics(String endpoint, boolean withinSLA, long responseTimeMs,
                                     String organizationId, int recordsReturned) {
        log.info("COMPLIANCE_METRICS - Endpoint: {}, WithinSLA: {}, ResponseTimeMs: {}, " +
                        "OrganizationId: {}, RecordsReturned: {}, Timestamp: {}",
                endpoint,
                withinSLA,
                responseTimeMs,
                organizationId,
                recordsReturned,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }
}
