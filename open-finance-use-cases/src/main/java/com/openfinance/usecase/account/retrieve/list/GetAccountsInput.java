package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.core.domain.account.AccountType;
import com.openfinance.usecase.pagination.IPaginationRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;

public record GetAccountsInput(
        @NotBlank(message = "Consent ID cannot be blank")
        String consentId,
        @NotBlank(message = "Organization ID cannot be blank")
        String organizationId,

        Optional<AccountType> type,

        @NotNull(message = "Page number cannot be null")
        @Min(value = 1, message = "Page number must be at least 1")
        int page,

        @NotNull(message = "Page size cannot be null")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 1000, message = "Page size must be at most 1000")
        int pageSize,

        @NotNull(message = "Account type cannot be null")
        AccountType accountType,

        Optional<String> paginationKey,

        @NotBlank(message = "X-FAPI-Interaction-ID cannot be blank")
        String xFapiInteractionId,

        Optional<String> xFapiAuthDate,
        Optional<String> xFapiCustomerIpAddress,
        Optional<String> xCustomerUserAgent
) implements IPaginationRequest {

    public GetAccountsInput {
        Objects.requireNonNull(consentId, "Consent ID cannot be null");
        Objects.requireNonNull(organizationId, "Organization ID cannot be null");
        Objects.requireNonNull(xFapiInteractionId, "X-FAPI-Interaction-ID cannot be null");

        if (type.isEmpty()) type = Optional.empty();
        if (paginationKey.isEmpty()) paginationKey = Optional.empty();
        if (xFapiAuthDate.isEmpty()) xFapiAuthDate = Optional.empty();
        if (xFapiCustomerIpAddress.isEmpty()) xFapiCustomerIpAddress = Optional.empty();
        if (xCustomerUserAgent.isEmpty()) xCustomerUserAgent = Optional.empty();
    }

    public boolean hasPaginationKey() {
        return paginationKey.isPresent() && !paginationKey.get().trim().isEmpty();
    }

    public static class Builder {
        private String consentId;
        private String organizationId;
        private Optional<AccountType> type = Optional.empty();
        private int page = 1;
        private int pageSize = 25;
        private AccountType accountType;
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

        public Builder type(AccountType type) {
            this.type = Optional.ofNullable(type);
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder accountType(AccountType accountType) {
            this.accountType = accountType;
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
            return new GetAccountsInput(consentId, organizationId, type, page, pageSize, accountType, paginationKey,
                    xFapiInteractionId, xFapiAuthDate, xFapiCustomerIpAddress, xCustomerUserAgent);
        }
    }
}
