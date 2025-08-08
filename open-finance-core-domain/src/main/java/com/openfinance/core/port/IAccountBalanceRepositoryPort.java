package com.openfinance.core.port;

import com.openfinance.core.domain.account.AccountBalance;
import com.openfinance.core.valueobjects.AccountId;

import java.util.Optional;

public interface IAccountBalanceRepositoryPort {
    /**
     * Finds account balance by account identifier
     *
     * @param accountId the account identifier
     * @return the account balance if found
     */
    Optional<AccountBalance> findByAccountId(AccountId accountId);

    /**
     * Checks if account balance exists for the given account
     *
     * @param accountId the account identifier
     * @return true if balance exists
     */
    boolean existsByAccountId(AccountId accountId);
}
