package com.openfinance.usecase.account.input;

import com.openfinance.core.enums.AccountType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Objects;
import java.util.Optional;

/**
 * Input data for the Get Accounts use case
 */
@Builder
public record GetAccountsInput(
        @NotBlank(message = "Consent ID cannot be blank")
        String consentId,

        @NotBlank(message = "Organization ID cannot be blank")
        String organizationId,

        Optional<AccountType> accountType,

        @NotNull(message = "Page cannot be null")
        @Min(value = 1, message = "Page must be greater than or equal to 1")
        Integer page,

        @NotNull(message = "Page size cannot be null")
        @Min(value = 1, message = "Page size must be greater than or equal to 1")
        @Max(value = 1000, message = "Page size must be less than or equal to 1000")
        Integer pageSize,

        Optional<String> paginationKey,

        @NotBlank(message = "X-FAPI-Interaction-ID cannot be blank")
        String xFapiInteractionId,

        Optional<String> xFapiAuthDate,
        Optional<String> xFapiCustomerIpAddress,
        Optional<String> xCustomerUserAgent
) {

    /**
     * Creates a new GetAccountsInput with validation
     */
    public GetAccountsInput {
        Objects.requireNonNull(consentId, "Consent ID cannot be null");
        Objects.requireNonNull(organizationId, "Organization ID cannot be null");
        Objects.requireNonNull(page, "Page cannot be null");
        Objects.requireNonNull(pageSize, "Page size cannot be null");
        Objects.requireNonNull(xFapiInteractionId, "X-FAPI-Interaction-ID cannot be null");

        // Ensure optional fields are not null
        if (accountType.isEmpty()) {
            accountType = Optional.empty();
        }
        if (paginationKey.isEmpty()) {
            paginationKey = Optional.empty();
        }
        if (xFapiAuthDate.isEmpty()) {
            xFapiAuthDate = Optional.empty();
        }
        if (xFapiCustomerIpAddress.isEmpty()) {
            xFapiCustomerIpAddress = Optional.empty();
        }
        if (xCustomerUserAgent.isEmpty()) {
            xCustomerUserAgent = Optional.empty();
        }
    }

    /**
     * Gets the zero-based page number for repository operations
     */
    public int getZeroBasedPage() {
        return page - 1;
    }

    /**
     * Checks if pagination key is present and valid
     */
    public boolean hasPaginationKey() {
        return paginationKey.isPresent() && !paginationKey.get().trim().isEmpty();
    }

    /**
     * Gets the effective page size respecting Open Finance limits
     */
    public int getEffectivePageSize() {
        // Open Finance requires minimum 25 items per page for non-first/last pages
        if (pageSize < 25 && page > 1) {
            return 25;
        }
        return Math.min(pageSize, 1000); // Maximum 1000 per page
    }
}
