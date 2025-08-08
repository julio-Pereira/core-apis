package com.openfinance.usecase.account.service;

import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.port.IPaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Service for handling operational limits according to Open Finance specifications
 * Manages monthly access limits per endpoint, client, and resource
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationalLimitsService {

    private final IPaginationService paginationService;

    // Open Finance operational limits per frequency category
    public enum OperationalLimitCategory {
        LOW_FREQUENCY(8, "Low Frequency"),           // 8 calls per month
        MEDIUM_FREQUENCY(30, "Medium Frequency"),    // 30 calls per month
        MEDIUM_HIGH_FREQUENCY(120, "Medium-High Frequency"), // 120 calls per month
        HIGH_FREQUENCY(240, "High Frequency"),       // 240 calls per month
        SPECIAL_ACCOUNTS(420, "Special Accounts");   // 420 calls per month for balances and limits

        private final int monthlyLimit;
        private final String description;

        OperationalLimitCategory(int monthlyLimit, String description) {
            this.monthlyLimit = monthlyLimit;
            this.description = description;
        }

        public int getMonthlyLimit() { return monthlyLimit; }
        public String getDescription() { return description; }
    }

    /**
     * Validates operational limits for the accounts endpoint
     * Accounts endpoint is classified as HIGH_FREQUENCY (240 calls/month)
     */
    public void validateAccountsOperationalLimit(String consentId, String customerId,
                                                 String organizationId, boolean isPaginationCall) {
        log.debug("Validating operational limits - ConsentId: {}, CustomerId: {}, OrganizationId: {}, " +
                "IsPaginationCall: {}", consentId, customerId, organizationId, isPaginationCall);

        // Pagination calls should not be counted towards operational limits
        if (isPaginationCall) {
            log.debug("Pagination call detected - skipping operational limit validation");
            return;
        }

        validateOperationalLimit(
                consentId,
                customerId,
                organizationId,
                "/accounts",
                OperationalLimitCategory.HIGH_FREQUENCY
        );
    }

    /**
     * Validates operational limits for a specific endpoint and category
     */
    public void validateOperationalLimit(String consentId, String customerId, String organizationId,
                                         String endpoint, OperationalLimitCategory category) {

        LimitKey limitKey = LimitKey.builder()
                .consentId(consentId)
                .customerId(customerId)
                .organizationId(organizationId)
                .endpoint(endpoint)
                .month(YearMonth.now())
                .build();

        log.debug("Checking operational limit for key: {}, category: {}, monthlyLimit: {}",
                limitKey, category.getDescription(), category.getMonthlyLimit());

        // Note: In a real implementation, this would check against a persistent store
        // For now, we simulate the validation
        int currentUsage = getCurrentMonthlyUsage(limitKey);

        if (currentUsage >= category.getMonthlyLimit()) {
            log.error("Operational limit exceeded - Key: {}, CurrentUsage: {}, MonthlyLimit: {}",
                    limitKey, currentUsage, category.getMonthlyLimit());

            throw new BusinessRuleViolationException(
                    String.format("Monthly operational limit exceeded for endpoint %s. " +
                                    "Limit: %d calls/month, Current usage: %d",
                            endpoint, category.getMonthlyLimit(), currentUsage)
            );
        }

        log.debug("Operational limit validation passed - CurrentUsage: {}, MonthlyLimit: {}",
                currentUsage, category.getMonthlyLimit());
    }

    /**
     * Records a successful API call for operational limits tracking
     * Only counts 2XX responses according to Open Finance specification
     */
    public void recordSuccessfulCall(String consentId, String customerId, String organizationId,
                                     String endpoint, boolean isPaginationCall) {

        // Pagination calls should not be counted
        if (isPaginationCall) {
            log.debug("Pagination call - not recording for operational limits");
            return;
        }

        LimitKey limitKey = LimitKey.builder()
                .consentId(consentId)
                .customerId(customerId)
                .organizationId(organizationId)
                .endpoint(endpoint)
                .month(YearMonth.now())
                .build();

        log.debug("Recording successful call for operational limits - Key: {}", limitKey);

        try {
            // Note: In a real implementation, this would increment the counter in persistent storage
            incrementUsageCounter(limitKey);
            log.trace("Successfully recorded operational limit usage");

        } catch (Exception e) {
            log.error("Failed to record operational limit usage for key: {}", limitKey, e);
            // Don't fail the main operation if recording fails
        }
    }

    /**
     * Determines if the current request is a pagination call
     * Based on presence of valid pagination-key parameter
     */
    public boolean isPaginationCall(String paginationKey) {
        if (paginationKey == null || paginationKey.trim().isEmpty()) {
            return false;
        }

        boolean isValid = paginationService.isValidPaginationKey(paginationKey);
        log.debug("Pagination key validation - Key present: true, IsValid: {}", isValid);

        return isValid;
    }

    /**
     * Gets operational limit status for monitoring
     */
    public OperationalLimitStatus getOperationalLimitStatus(String consentId, String customerId,
                                                            String organizationId, String endpoint) {
        LimitKey limitKey = LimitKey.builder()
                .consentId(consentId)
                .customerId(customerId)
                .organizationId(organizationId)
                .endpoint(endpoint)
                .month(YearMonth.now())
                .build();

        try {
            int currentUsage = getCurrentMonthlyUsage(limitKey);
            OperationalLimitCategory category = getCategoryForEndpoint(endpoint);

            return OperationalLimitStatus.builder()
                    .limitKey(limitKey)
                    .currentUsage(currentUsage)
                    .monthlyLimit(category.getMonthlyLimit())
                    .remainingCalls(Math.max(0, category.getMonthlyLimit() - currentUsage))
                    .category(category)
                    .withinLimits(currentUsage < category.getMonthlyLimit())
                    .checkTimestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get operational limit status for key: {}", limitKey, e);
            return OperationalLimitStatus.builder()
                    .limitKey(limitKey)
                    .currentUsage(0)
                    .monthlyLimit(0)
                    .remainingCalls(0)
                    .withinLimits(false)
                    .checkTimestamp(LocalDateTime.now())
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Gets the appropriate category for an endpoint
     */
    private OperationalLimitCategory getCategoryForEndpoint(String endpoint) {
        return switch (endpoint) {
            case "/accounts" -> OperationalLimitCategory.HIGH_FREQUENCY;
            case "/accounts/{accountId}/balances", "/accounts/{accountId}/overdraft-limits" ->
                    OperationalLimitCategory.SPECIAL_ACCOUNTS;
            case "/accounts/{accountId}/transactions", "/accounts/{accountId}/transactions-current" ->
                    OperationalLimitCategory.HIGH_FREQUENCY;
            case "/accounts/{accountId}" -> OperationalLimitCategory.MEDIUM_HIGH_FREQUENCY;
            default -> OperationalLimitCategory.MEDIUM_FREQUENCY;
        };
    }

    /**
     * Simulates getting current monthly usage from persistent storage
     * In a real implementation, this would query MongoDB or Redis
     */
    private int getCurrentMonthlyUsage(LimitKey limitKey) {
        // Simulation - in real implementation would query from database
        log.trace("Getting current monthly usage for key: {}", limitKey);
        return 0; // Placeholder
    }

    /**
     * Simulates incrementing the usage counter in persistent storage
     */
    private void incrementUsageCounter(LimitKey limitKey) {
        // Simulation - in real implementation would increment in database
        log.trace("Incrementing usage counter for key: {}", limitKey);
    }

    /**
     * Record representing the key for operational limits tracking
     */
    @lombok.Builder
    public record LimitKey(
            String consentId,
            String customerId,
            String organizationId,
            String endpoint,
            YearMonth month
    ) {}

    /**
     * Record representing operational limit status
     */
    @lombok.Builder
    public record OperationalLimitStatus(
            LimitKey limitKey,
            int currentUsage,
            int monthlyLimit,
            int remainingCalls,
            OperationalLimitCategory category,
            boolean withinLimits,
            LocalDateTime checkTimestamp,
            String error
    ) {
        public boolean hasError() {
            return error != null && !error.trim().isEmpty();
        }

        public double getUsagePercentage() {
            if (monthlyLimit == 0) return 0.0;
            return (double) currentUsage / monthlyLimit * 100.0;
        }
    }
}
