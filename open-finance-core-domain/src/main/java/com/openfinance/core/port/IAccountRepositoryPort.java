package com.openfinance.core.port;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountType;
import com.openfinance.core.domain.account.AccountId;

import java.util.List;
import java.util.Optional;

public interface IAccountRepositoryPort {
    /**
     * Finds an account by its identifier
     *
     * @param accountId the account identifier
     * @return the account if found
     */
    Optional<Account> findById(AccountId accountId);

    /**
     * Finds all accounts with optional filtering by account type
     *
     * @param accountType optional account type filter
     * @param page page number (0-based)
     * @param size page size
     * @return list of accounts
     */
    List<Account> findAll(Optional<AccountType> accountType, int page, int size);

    /**
     * Counts total accounts with optional filtering by account type
     *
     * @param accountType optional account type filter
     * @return total count of accounts
     */
    long count(Optional<AccountType> accountType);

    /**
     * Finds accounts by company CNPJ
     *
     * @param companyCnpj the company CNPJ
     * @return list of accounts for the company
     */
    List<Account> findByCompanyCnpj(String companyCnpj);

    /**
     * Checks if an account exists
     *
     * @param accountId the account identifier
     * @return true if account exists
     */
    boolean existsById(AccountId accountId);
}

