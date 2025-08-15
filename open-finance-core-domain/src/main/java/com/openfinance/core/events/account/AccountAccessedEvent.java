package com.openfinance.core.events.account;

import com.openfinance.core.events.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event for account access.
 *
 * Records all relevant information for audit and compliance
 * according to Open Finance Brazil specifications, including:
 * - Consent and permission details
 * - Applied filtering information
 * - Request context (FAPI headers)
 * - Performance metrics
 */
public class AccountAccessedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String aggregateId; // consentId
    private final String eventType;

    // Consent data
    private final String consentId;
    private final String organizationId;
    private final List<String> originalPermissions;
    private final List<String> filteredPermissions;
    private final List<String> removedPermissions;

    // Operation context
    private final String operation;
    private final String endpoint;
    private final AccountAccessContext accessContext;

    // Operation results
    private final AccountAccessResult accessResult;

    // Compliance and audit
    private final ComplianceInfo complianceInfo;

    private AccountAccessedEvent(Builder builder) {
        this.eventId = Objects.requireNonNull(builder.eventId, "Event ID cannot be null");
        this.occurredOn = Objects.requireNonNull(builder.occurredOn, "Occurred on cannot be null");
        this.aggregateId = Objects.requireNonNull(builder.consentId, "Consent ID cannot be null");
        this.eventType = "AccountAccessed";

        this.consentId = builder.consentId;
        this.organizationId = Objects.requireNonNull(builder.organizationId, "Organization ID cannot be null");
        this.originalPermissions = List.copyOf(builder.originalPermissions != null ? builder.originalPermissions : List.of());
        this.filteredPermissions = List.copyOf(builder.filteredPermissions != null ? builder.filteredPermissions : List.of());
        this.removedPermissions = List.copyOf(builder.removedPermissions != null ? builder.removedPermissions : List.of());

        this.operation = Objects.requireNonNull(builder.operation, "Operation cannot be null");
        this.endpoint = Objects.requireNonNull(builder.endpoint, "Endpoint cannot be null");
        this.accessContext = Objects.requireNonNull(builder.accessContext, "Access context cannot be null");

        this.accessResult = Objects.requireNonNull(builder.accessResult, "Access result cannot be null");
        this.complianceInfo = Objects.requireNonNull(builder.complianceInfo, "Compliance info cannot be null");
    }

    // Getters
    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    public String getConsentId() {
        return consentId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public List<String> getOriginalPermissions() {
        return originalPermissions;
    }

    public List<String> getFilteredPermissions() {
        return filteredPermissions;
    }

    public List<String> getRemovedPermissions() {
        return removedPermissions;
    }

    public String getOperation() {
        return operation;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public AccountAccessContext getAccessContext() {
        return accessContext;
    }

    public AccountAccessResult getAccessResult() {
        return accessResult;
    }

    public ComplianceInfo getComplianceInfo() {
        return complianceInfo;
    }

    /**
     * Checks if permissions were filtered
     */
    public boolean wasPermissionFiltered() {
        return !removedPermissions.isEmpty();
    }

    /**
     * Checks if the operation was successful
     */
    public boolean isSuccessful() {
        return accessResult.isSuccess();
    }

    /**
     * Checks if it is within SLA
     */
    public boolean isWithinSLA() {
        return complianceInfo.isWithinSLA();
    }

    /**
     * Returns the number of accessed accounts
     */
    public int getAccountCount() {
        return accessResult.getAccountCount();
    }

    /**
     * Builder for event construction
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private LocalDateTime occurredOn = LocalDateTime.now();
        private String consentId;
        private String organizationId;
        private List<String> originalPermissions;
        private List<String> filteredPermissions;
        private List<String> removedPermissions;
        private String operation;
        private String endpoint;
        private AccountAccessContext accessContext;
        private AccountAccessResult accessResult;
        private ComplianceInfo complianceInfo;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder occurredOn(LocalDateTime occurredOn) {
            this.occurredOn = occurredOn;
            return this;
        }

        public Builder consentId(String consentId) {
            this.consentId = consentId;
            return this;
        }

        public Builder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder originalPermissions(List<String> originalPermissions) {
            this.originalPermissions = originalPermissions;
            return this;
        }

        public Builder filteredPermissions(List<String> filteredPermissions) {
            this.filteredPermissions = filteredPermissions;
            return this;
        }

        public Builder removedPermissions(List<String> removedPermissions) {
            this.removedPermissions = removedPermissions;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder accessContext(AccountAccessContext accessContext) {
            this.accessContext = accessContext;
            return this;
        }

        public Builder accessResult(AccountAccessResult accessResult) {
            this.accessResult = accessResult;
            return this;
        }

        public Builder complianceInfo(ComplianceInfo complianceInfo) {
            this.complianceInfo = complianceInfo;
            return this;
        }

        public AccountAccessedEvent build() {
            return new AccountAccessedEvent(this);
        }
    }

    /**
     * Record with access context (FAPI headers and parameters)
     */
    public record AccountAccessContext(
            String xFapiInteractionId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xCustomerUserAgent,
            PaginationContext paginationContext,
            FilterContext filterContext
    ) {
        public AccountAccessContext {
            Objects.requireNonNull(xFapiInteractionId, "X-FAPI-Interaction-ID cannot be null");
        }

        /**
         * Pagination context
         */
        public record PaginationContext(
                int page,
                int pageSize,
                String paginationKey,
                boolean hasPaginationKey
        ) {}

        /**
         * Context of applied filters
         */
        public record FilterContext(
                String accountTypeFilter,
                boolean hasTypeFilter,
                LocalDateTime transactionFromDate,
                LocalDateTime transactionToDate,
                boolean hasDateFilter
        ) {}
    }

    /**
     * Record with access result
     */
    public record AccountAccessResult(
            boolean success,
            int accountCount,
            long executionTimeMs,
            String errorCode,
            String errorMessage,
            AccessResultType resultType
    ) {
        public AccountAccessResult {
            if (success && accountCount < 0) {
                throw new IllegalArgumentException("Account count cannot be negative for successful operations");
            }
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean hasError() {
            return !success;
        }

        public int getAccountCount() {
            return accountCount;
        }

        /**
         * Access result types
         */
        public enum AccessResultType {
            SUCCESS_WITH_DATA,
            SUCCESS_EMPTY_RESULT,
            ERROR_VALIDATION,
            ERROR_PERMISSION,
            ERROR_RATE_LIMIT,
            ERROR_TECHNICAL
        }
    }

    /**
     * Record with compliance and audit information
     */
    public record ComplianceInfo(
            boolean withinSLA,
            long slaThresholdMs,
            boolean rateLimitChecked,
            long remainingRateLimit,
            AuditLevel auditLevel,
            SecurityContext securityContext
    ) {
        public boolean isWithinSLA() {
            return withinSLA;
        }

        public boolean isRateLimitNearExhaustion() {
            return remainingRateLimit <= 10; // Less than 10 requests remaining
        }

        /**
         * Audit levels
         */
        public enum AuditLevel {
            BASIC,      // Basic audit
            DETAILED,   // Detailed audit
            SENSITIVE,  // Sensitive data
            CRITICAL    // Critical operation
        }

        /**
         * Security context
         */
        public record SecurityContext(
                boolean consentValid,
                boolean permissionsValid,
                boolean ipAddressAllowed,
                boolean suspiciousActivity,
                List<String> securityFlags
        ) {
            public boolean hasSecurityIssues() {
                return suspiciousActivity || !ipAddressAllowed ||
                        (securityFlags != null && !securityFlags.isEmpty());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountAccessedEvent that = (AccountAccessedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "AccountAccessedEvent{" +
                "eventId='" + eventId + '\'' +
                ", consentId='" + consentId + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", operation='" + operation + '\'' +
                ", success=" + accessResult.success +
                ", accountCount=" + accessResult.accountCount +
                ", permissionsFiltered=" + wasPermissionFiltered() +
                ", occurredOn=" + occurredOn +
                '}';
    }
}