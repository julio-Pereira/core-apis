package com.openfinance.usecase.account.aspect;

import com.openfinance.usecase.account.retrieve.list.GetAccountsInput;
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

    public void logAccountAccessStart(GetAccountsInput input) {
        log.info("ACCOUNT_ACCESS_START - ConsentId: {}, OrganizationId: {}, Operation: GET_ACCOUNTS, " +
                        "Page: {}, PageSize: {}, AccountTypeFilter: {}, InteractionId: {}, Timestamp: {}",
                input.consentId(),
                input.organizationId(),
                input.page(),
                input.pageSize(),
                input.accountType(),
                input.xFapiInteractionId(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

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

    public void logConsentValidation(String consentId, boolean isValid, boolean hasPermission) {
        log.debug("CONSENT_VALIDATION - ConsentId: {}, IsValid: {}, HasAccountsReadPermission: {}, Timestamp: {}",
                consentId,
                isValid,
                hasPermission,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

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

    public void logExternalServiceCall(String consentId, String operation, boolean success, long responseTimeMs) {
        if (success) {
            log.debug("EXTERNAL_SERVICE_CALL - ConsentId: {}, Operation: {}, Status: SUCCESS, ResponseTimeMs: {}, Timestamp: {}",
                    consentId,
                    operation,
                    responseTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            log.warn("EXTERNAL_SERVICE_CALL - ConsentId: {}, Operation: {}, Status: FAILURE, ResponseTimeMs: {}, Timestamp: {}",
                    consentId,
                    operation,
                    responseTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    public void logComplianceMetrics(String endpoint, boolean withinSLA, long executionTimeMs,
                                     String organizationId, int recordCount) {
        log.info("COMPLIANCE_METRICS - Endpoint: {}, OrganizationId: {}, WithinSLA: {}, " +
                        "ExecutionTimeMs: {}, RecordCount: {}, Timestamp: {}",
                endpoint,
                organizationId,
                withinSLA,
                executionTimeMs,
                recordCount,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));

        // Log adicional para violações de SLA
        if (!withinSLA) {
            log.warn("SLA_VIOLATION - Endpoint: {}, OrganizationId: {}, ExecutionTimeMs: {}, Timestamp: {}",
                    endpoint,
                    organizationId,
                    executionTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    public void logPerformanceMetrics(String operationName, long totalExecutionTimeMs,
                                      long validationTimeMs, long processingTimeMs, long externalCallTimeMs) {
        log.debug("PERFORMANCE_METRICS - Operation: {}, TotalTimeMs: {}, ValidationTimeMs: {}, " +
                        "ProcessingTimeMs: {}, ExternalCallTimeMs: {}, Timestamp: {}",
                operationName,
                totalExecutionTimeMs,
                validationTimeMs,
                processingTimeMs,
                externalCallTimeMs,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));

        // Log de warning para operações lentas
        if (totalExecutionTimeMs > 1000) {
            log.warn("SLOW_OPERATION - Operation: {}, ExecutionTimeMs: {}, Timestamp: {}",
                    operationName,
                    totalExecutionTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }

    /**
     * Registra evento de segurança
     */
    public void logSecurityEvent(String eventType, String consentId, String organizationId,
                                 String details, String interactionId) {
        log.warn("SECURITY_EVENT - EventType: {}, ConsentId: {}, OrganizationId: {}, " +
                        "Details: {}, InteractionId: {}, Timestamp: {}",
                eventType,
                consentId,
                organizationId,
                details,
                interactionId,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    public void logBusinessRuleViolation(String ruleType, String consentId, String organizationId,
                                         String violation, String interactionId) {
        log.error("BUSINESS_RULE_VIOLATION - RuleType: {}, ConsentId: {}, OrganizationId: {}, " +
                        "Violation: {}, InteractionId: {}, Timestamp: {}",
                ruleType,
                consentId,
                organizationId,
                violation,
                interactionId,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    public void logAuditEvent(String eventType, String userId, String resource,
                              String action, String result, String details) {
        log.info("AUDIT_EVENT - EventType: {}, UserId: {}, Resource: {}, Action: {}, " +
                        "Result: {}, Details: {}, Timestamp: {}",
                eventType,
                userId,
                resource,
                action,
                result,
                details,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Registra métricas de utilização da API
     */
    public void logApiUsageMetrics(String organizationId, String endpoint, int requestCount,
                                   long avgResponseTimeMs, int errorCount) {
        log.info("API_USAGE_METRICS - OrganizationId: {}, Endpoint: {}, RequestCount: {}, " +
                        "AvgResponseTimeMs: {}, ErrorCount: {}, Timestamp: {}",
                organizationId,
                endpoint,
                requestCount,
                avgResponseTimeMs,
                errorCount,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
    }

    /**
     * Registra informações de health check para monitoramento
     */
    public void logHealthCheck(String component, boolean healthy, String details, long checkTimeMs) {
        if (healthy) {
            log.debug("HEALTH_CHECK - Component: {}, Status: HEALTHY, Details: {}, CheckTimeMs: {}, Timestamp: {}",
                    component,
                    details,
                    checkTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        } else {
            log.error("HEALTH_CHECK - Component: {}, Status: UNHEALTHY, Details: {}, CheckTimeMs: {}, Timestamp: {}",
                    component,
                    details,
                    checkTimeMs,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }
}
