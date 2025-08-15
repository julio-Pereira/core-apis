package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.core.domain.account.Account;
import com.openfinance.usecase.pagination.IPaginationRequest;
import com.openfinance.usecase.pagination.PaginationInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record GetAccountsOutput(
        List<AccountOutputDto> accounts,
        PaginationInfo paginationInfo,
        LocalDateTime requestDateTime
) {

    public GetAccountsOutput {
        Objects.requireNonNull(accounts, "Accounts list cannot be null");
        Objects.requireNonNull(paginationInfo, "Pagination info cannot be null");
        Objects.requireNonNull(requestDateTime, "Request date time cannot be null");
    }

    public boolean hasAccounts() {
        return accounts != null && !accounts.isEmpty();
    }

    public int getAccountsCount() {
        return accounts.size();
    }

    /**
     * Cria uma instância vazia para casos de erro ou quando não há contas
     */
    public static GetAccountsOutput empty(PaginationInfo paginationInfo) {
        return new GetAccountsOutput(
                List.of(),
                paginationInfo,
                LocalDateTime.now()
        );
    }

}

