package com.openfinance.usecase.account.service;

import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.port.IRateLimitService;
import com.openfinance.usecase.utils.FrequencyCategory;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for handling rate limit validations according to Open Finance specifications
 * Implements TPM (Transactions Per Minute) and TPS (Transactions Per Second) controls
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitValidationService {

    private final IRateLimitService rateLimitService;

    /**
     * Validates rate limits for the accounts endpoint (High Frequency category)
     */
    public void validateAccountsEndpointRateLimit(String organizationId) {
        validateRateLimit(organizationId, "/accounts", FrequencyCategory.HIGH);
    }

    /**
     * Validates rate limits for a specific organization and endpoint
     */
    public void validateRateLimit(String organizationId, String endpoint, FrequencyCategory category) {
        log.debug("Validating rate limit for organizationId: {}, endpoint: {}, category: {}",
                organizationId, endpoint, category.getDescription());

        boolean withinLimits = rateLimitService.isWithinRateLimit(organizationId, endpoint);

        if (!withinLimits) {
            long remainingRequests = rateLimitService.getRemainingRequests(organizationId, endpoint);

            log.error("Rate limit exceeded - OrganizationId: {}, Endpoint: {}, Category: {}, " +
                            "BaseTPM: {}, RemainingRequests: {}",
                    organizationId, endpoint, category.getDescription(),
                    category.getBaseTPM(), remainingRequests);

            throw new BusinessRuleViolationException(
                    String.format("Rate limit exceeded for endpoint %s. Category: %s, Base TPM: %d",
                            endpoint, category.getDescription(), category.getBaseTPM())
            );
        }

        log.debug("Rate limit validation passed for organizationId: {}, endpoint: {}",
                organizationId, endpoint);
    }

    /**
     * Records a successful request for rate limiting tracking
     */
    public void recordSuccessfulRequest(String organizationId, String endpoint) {
        log.debug("Recording successful request for organizationId: {}, endpoint: {}",
                organizationId, endpoint);

        try {
            rateLimitService.recordRequest(organizationId, endpoint);
            log.trace("Request recorded successfully for rate limiting");

        } catch (Exception e) {
            log.error("Failed to record request for rate limiting - OrganizationId: {}, Endpoint: {}",
                    organizationId, endpoint, e);
            // Don't fail the main operation if rate limit recording fails
        }
    }

    /**
     * Gets remaining requests for monitoring purposes
     */
    public RateLimitStatus getRateLimitStatus(String organizationId, String endpoint) {
        try {
            long remainingRequests = rateLimitService.getRemainingRequests(organizationId, endpoint);
            boolean withinLimits = rateLimitService.isWithinRateLimit(organizationId, endpoint);

            return RateLimitStatus.builder()
                    .organizationId(organizationId)
                    .endpoint(endpoint)
                    .remainingRequests(remainingRequests)
                    .withinLimits(withinLimits)
                    .checkTimestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get rate limit status for organizationId: {}, endpoint: {}",
                    organizationId, endpoint, e);

            return RateLimitStatus.builder()
                    .organizationId(organizationId)
                    .endpoint(endpoint)
                    .remainingRequests(0)
                    .withinLimits(false)
                    .checkTimestamp(java.time.LocalDateTime.now())
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Calculates TPM limit based on consent count for high frequency endpoints
     */
    public int calculateTPMLimit(long consentCount) {
        if (consentCount <= 1_000_000) {
            return 2_500;
        } else if (consentCount <= 2_000_000) {
            return 5_000;
        } else if (consentCount <= 3_000_000) {
            return 8_000;
        } else if (consentCount <= 6_000_000) {
            return 10_000;
        } else {
            // For QCA > 6 million, calculated in 2 million ranges, adding 2,000 to previous limit
            long excessMillions = (consentCount - 6_000_000) / 2_000_000;
            return (int) (10_000 + (excessMillions * 2_000));
        }
    }

    /**
     * Validates if organization is within global TPS limits (300 TPS minimum)
     */
    public void validateGlobalTPSLimit(String organizationId) {
        log.debug("Validating global TPS limit for organizationId: {}", organizationId);

        // Note: This would typically integrate with infrastructure monitoring
        // For now, we delegate to the rate limit service
        boolean withinGlobalLimits = rateLimitService.isWithinRateLimit(organizationId, "GLOBAL_TPS");

        if (!withinGlobalLimits) {
            log.error("Global TPS limit exceeded for organizationId: {}", organizationId);
            throw new BusinessRuleViolationException(
                    "Global TPS limit exceeded. Service is overloaded."
            );
        }

        log.debug("Global TPS validation passed for organizationId: {}", organizationId);
    }

    /**
     * Record for rate limit status information
     */
    @Builder
    public record RateLimitStatus(
            String organizationId,
            String endpoint,
            long remainingRequests,
            boolean withinLimits,
            java.time.LocalDateTime checkTimestamp,
            String error
    ) {
        public boolean hasError() {
            return error != null && !error.trim().isEmpty();
        }
    }
}
