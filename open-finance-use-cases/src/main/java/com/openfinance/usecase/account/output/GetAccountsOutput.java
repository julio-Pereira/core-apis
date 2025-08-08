package com.openfinance.usecase.account.output;

import com.openfinance.core.domain.account.Account;
import com.openfinance.usecase.pagination.PaginationInfo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Output data for the Get Accounts use case
 */
@Builder
public record GetAccountsOutput(
        List<Account> accounts,
        PaginationInfo paginationInfo,
        LocalDateTime requestDateTime
) {

    /**
     * Creates a new GetAccountsOutput with validation
     */
    public GetAccountsOutput {
        Objects.requireNonNull(accounts, "Accounts list cannot be null");
        Objects.requireNonNull(paginationInfo, "Pagination info cannot be null");
        Objects.requireNonNull(requestDateTime, "Request date time cannot be null");
    }

    /**
     * Checks if there are any accounts in the result
     */
    public boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    /**
     * Gets the total number of accounts in this page
     */
    public int getAccountCount() {
        return accounts.size();
    }
}
