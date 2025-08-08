package com.openfinance.core.port;

import java.util.Optional;

/**
 * Service port for pagination key generation and validation
 */
public interface IPaginationService {

    /**
     * Generates a pagination key for the current request
     *
     * @param requestIdentifier unique request identifier
     * @param currentPage current page number
     * @param pageSize page size
     * @return pagination key
     */
    String generatePaginationKey(String requestIdentifier, int currentPage, int pageSize);

    /**
     * Validates a pagination key
     *
     * @param paginationKey the pagination key to validate
     * @return true if valid
     */
    boolean isValidPaginationKey(String paginationKey);

    /**
     * Extracts request information from pagination key
     *
     * @param paginationKey the pagination key
     * @return request identifier if valid
     */
    Optional<String> extractRequestIdentifier(String paginationKey);
}
