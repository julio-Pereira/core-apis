package com.openfinance.usecase.account.output;

import com.openfinance.core.domain.account.Account;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Output data for the Get Account By ID use case
 * Represents the response for specific account identification data
 */
@Builder
public record GetAccountByIdOutput(
        Account account,
        LinkInfo linkInfo,
        LocalDateTime requestDateTime
) {

    /**
     * Creates a new GetAccountByIdOutput with validation
     */
    public GetAccountByIdOutput {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(linkInfo, "Link info cannot be null");
        Objects.requireNonNull(requestDateTime, "Request date time cannot be null");
    }

    /**
     * Link information for the account identification response
     * Contains self-reference link as required by Open Finance specification
     */
    public record LinkInfo(
            String selfLink
    ) {

        /**
         * Creates a new LinkInfo with validation
         */
        public LinkInfo {
            Objects.requireNonNull(selfLink, "Self link cannot be null");
            if (selfLink.trim().isEmpty()) {
                throw new IllegalArgumentException("Self link cannot be empty");
            }
        }
    }
}