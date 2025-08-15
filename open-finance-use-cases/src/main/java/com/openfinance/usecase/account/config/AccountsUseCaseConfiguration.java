package com.openfinance.usecase.account.config;

import com.openfinance.usecase.pagination.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Accounts Use Cases
 * Defines beans and configurations specific to account operations
 */
@Slf4j
@Configuration
public class AccountsUseCaseConfiguration {

    @Value("${open-finance.api.base-url:https://api.banco.com.br}")
    private String baseUrl;

    @Value("${open-finance.pagination.default-page-size:25}")
    private int defaultPageSize;

    @Value("${open-finance.pagination.max-page-size:1000}")
    private int maxPageSize;

    @Value("${open-finance.pagination.key-expiration-minutes:60}")
    private int paginationKeyExpirationMinutes;

    /**
     * Creates the pagination link builder service with configured base URL
     */
    @Bean
    public Pagination paginationLinkBuilderService() {
        log.info("Configuring PaginationLinkBuilderService with baseUrl: {}", baseUrl);

        Pagination service = new Pagination(baseUrl);

        if (!service.isValidBaseUrl()) {
            log.warn("Invalid base URL configured: {}", baseUrl);
        }

        return service;
    }

    /**
     * Gets the default page size for pagination
     */
    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    /**
     * Gets the maximum allowed page size
     */
    public int getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Gets the pagination key expiration time in minutes
     */
    public int getPaginationKeyExpirationMinutes() {
        return paginationKeyExpirationMinutes;
    }

    /**
     * Gets the configured base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Validates configuration values
     */
    @jakarta.annotation.PostConstruct
    public void validateConfiguration() {
        log.info("Validating Accounts Use Case configuration...");

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Base URL must be configured");
        }

        if (defaultPageSize < 1 || defaultPageSize > maxPageSize) {
            throw new IllegalStateException("Default page size must be between 1 and " + maxPageSize);
        }

        if (maxPageSize > 1000) {
            log.warn("Max page size {} exceeds Open Finance recommended maximum of 1000", maxPageSize);
        }

        if (paginationKeyExpirationMinutes < 1 || paginationKeyExpirationMinutes > 120) {
            log.warn("Pagination key expiration {} minutes is outside recommended range of 1-120 minutes",
                    paginationKeyExpirationMinutes);
        }

        log.info("Accounts Use Case configuration validated successfully - BaseUrl: {}, " +
                        "DefaultPageSize: {}, MaxPageSize: {}, PaginationKeyExpirationMinutes: {}",
                baseUrl, defaultPageSize, maxPageSize, paginationKeyExpirationMinutes);
    }
}