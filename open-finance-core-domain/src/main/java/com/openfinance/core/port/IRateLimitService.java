package com.openfinance.core.port;

/**
 * Service port for rate limiting operations
 */
public interface IRateLimitService {

    /**
     * Checks if the request is within rate limits
     *
     * @param identifier request identifier (IP, organizationId, etc.)
     * @param endpoint the endpoint being accessed
     * @return true if within limits
     */
    boolean isWithinRateLimit(String identifier, String endpoint);

    /**
     * Records a request for rate limiting purposes
     *
     * @param identifier request identifier
     * @param endpoint the endpoint being accessed
     */
    void recordRequest(String identifier, String endpoint);

    /**
     * Gets remaining requests for the current period
     *
     * @param identifier request identifier
     * @param endpoint the endpoint being accessed
     * @return remaining requests
     */
    long getRemainingRequests(String identifier, String endpoint);
}
