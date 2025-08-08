package com.openfinance.usecase.account.input;

import com.openfinance.core.enums.AccountType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Input data for the Get Accounts use case
 */
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
     * Creates a builder for GetAccountsInput
     */
    public static Builder builder() {
        return new Builder();
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
     * Builder pattern for GetAccountsInput
     */
    public static class Builder {
        private String consentId;
        private String organizationId;
        private Optional<AccountType> accountType = Optional.empty();
        private Integer page = 1;
        private Integer pageSize = 25;
        private Optional<String> paginationKey = Optional.empty();
        private String xFapiInteractionId;
        private Optional<String> xFapiAuthDate = Optional.empty();
        private Optional<String> xFapiCustomerIpAddress = Optional.empty();
        private Optional<String> xCustomerUserAgent = Optional.empty();

        public Builder consentId(String consentId) {
            this.consentId = consentId;
            return this;
        }

        public Builder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder accountType(AccountType accountType) {
            this.accountType = Optional.ofNullable(accountType);
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder paginationKey(String paginationKey) {
            this.paginationKey = Optional.ofNullable(paginationKey);
            return this;
        }

        public Builder xFapiInteractionId(String xFapiInteractionId) {
            this.xFapiInteractionId = xFapiInteractionId;
            return this;
        }

        public Builder xFapiAuthDate(String xFapiAuthDate) {
            this.xFapiAuthDate = Optional.ofNullable(xFapiAuthDate);
            return this;
        }

        public Builder xFapiCustomerIpAddress(String xFapiCustomerIpAddress) {
            this.xFapiCustomerIpAddress = Optional.ofNullable(xFapiCustomerIpAddress);
            return this;
        }

        public Builder xCustomerUserAgent(String xCustomerUserAgent) {
            this.xCustomerUserAgent = Optional.ofNullable(xCustomerUserAgent);
            return this;
        }

        public GetAccountsInput build() {
            return new GetAccountsInput(
                    consentId,
                    organizationId,
                    accountType,
                    page,
                    pageSize,
                    paginationKey,
                    xFapiInteractionId,
                    xFapiAuthDate,
                    xFapiCustomerIpAddress,
                    xCustomerUserAgent
            );
        }
    }
}
