package com.openfinance.core.port;

import com.openfinance.core.domain.transaction.OverdraftLimits;
import com.openfinance.core.domain.account.AccountId;

import java.util.Optional;

/**
 * Repository port for OverdraftLimits entity operations
 */
public interface IOverdraftLimitsRepository {

    /**
     * Finds overdraft limits by account identifier
     *
     * @param accountId the account identifier
     * @return the overdraft limits if found
     */
    Optional<OverdraftLimits> findByAccountId(AccountId accountId);

    /**
     * Checks if overdraft limits exist for the given account
     *
     * @param accountId the account identifier
     * @return true if overdraft limits exist
     */
    boolean existsByAccountId(AccountId accountId);
}
