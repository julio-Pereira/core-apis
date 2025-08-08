package com.openfinance.usecase.account.input;

import com.openfinance.core.valueobjects.AccountId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Objects;
import java.util.Optional;

/**
 * Input data for the Get Account By ID use case
 * Represents a request for specific account identification data
 */
@Builder
public record GetAccountByIdInput(
        @NotNull(message = "Account ID cannot be null")
        AccountId accountId,

        @NotBlank(message = "Consent ID cannot be blank")
        String consentId,

        @NotBlank(message = "Organization ID cannot be blank")
        String organizationId,

        @NotBlank(message = "X-FAPI-Interaction-ID cannot be blank")
        String xFapiInteractionId,

        Optional<String> xFapiAuthDate,
        Optional<String> xFapiCustomerIpAddress,
        Optional<String> xCustomerUserAgent
) {

    /**
     * Creates a new GetAccountByIdInput with validation
     */
    public GetAccountByIdInput {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(consentId, "Consent ID cannot be null");
        Objects.requireNonNull(organizationId, "Organization ID cannot be null");
        Objects.requireNonNull(xFapiInteractionId, "X-FAPI-Interaction-ID cannot be null");

        // Ensure optional fields are not null
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
}