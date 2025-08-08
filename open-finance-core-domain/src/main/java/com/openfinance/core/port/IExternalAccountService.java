package com.openfinance.core.port;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountBalance;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.enums.CreditDebitIndicator;
import com.openfinance.core.domain.transaction.OverdraftLimits;
import com.openfinance.core.domain.transaction.Transaction;
import com.openfinance.core.valueobjects.AccountId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * External service port for fetching account data from external systems
 */
public interface IExternalAccountService {

    /**
     * Fetches accounts from external system
     *
     * @param consentId the consent identifier
     * @param accountType optional account type filter
     * @param page page number
     * @param size page size
     * @return list of accounts
     */
    List<Account> fetchAccounts(String consentId,
                                Optional<AccountType> accountType,
                                int page,
                                int size);

    /**
     * Fetches account details from external system
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @return account details if found
     */
    Optional<Account> fetchAccountById(String consentId, AccountId accountId);

    /**
     * Fetches account balance from external system
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @return account balance if found
     */
    Optional<AccountBalance> fetchAccountBalance(String consentId, AccountId accountId);

    /**
     * Fetches transactions from external system
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @param fromBookingDate optional start date filter
     * @param toBookingDate optional end date filter
     * @param creditDebitIndicator optional credit/debit filter
     * @param page page number
     * @param size page size
     * @return list of transactions
     */
    List<Transaction> fetchTransactions(String consentId,
                                        AccountId accountId,
                                        Optional<LocalDate> fromBookingDate,
                                        Optional<LocalDate> toBookingDate,
                                        Optional<CreditDebitIndicator> creditDebitIndicator,
                                        int page,
                                        int size);

    /**
     * Fetches recent transactions (last 7 days) from external system
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @param fromBookingDate optional start date filter (max 7 days ago)
     * @param toBookingDate optional end date filter
     * @param creditDebitIndicator optional credit/debit filter
     * @param page page number
     * @param size page size
     * @return list of recent transactions
     */
    List<Transaction> fetchRecentTransactions(String consentId,
                                              AccountId accountId,
                                              Optional<LocalDate> fromBookingDate,
                                              Optional<LocalDate> toBookingDate,
                                              Optional<CreditDebitIndicator> creditDebitIndicator,
                                              int page,
                                              int size);

    /**
     * Fetches overdraft limits from external system
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @return overdraft limits if found
     */
    Optional<OverdraftLimits> fetchOverdraftLimits(String consentId, AccountId accountId);
}