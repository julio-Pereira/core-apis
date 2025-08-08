package com.openfinance.core.port;

import com.openfinance.core.enums.CreditDebitIndicator;
import com.openfinance.core.domain.transaction.Transaction;
import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.TransactionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITransactionRepositoryPort {
    /**
     * Finds a transaction by its identifier
     *
     * @param transactionId the transaction identifier
     * @return the transaction if found
     */
    Optional<Transaction> findById(TransactionId transactionId);

    /**
     * Finds transactions by account with optional filters
     *
     * @param accountId the account identifier
     * @param fromBookingDate optional start date filter
     * @param toBookingDate optional end date filter
     * @param creditDebitIndicator optional credit/debit filter
     * @param page page number (0-based)
     * @param size page size
     * @return list of transactions
     */
    List<Transaction> findByAccountId(AccountId accountId,
                                      Optional<LocalDate> fromBookingDate,
                                      Optional<LocalDate> toBookingDate,
                                      Optional<CreditDebitIndicator> creditDebitIndicator,
                                      int page,
                                      int size);

    /**
     * Finds recent transactions (last 7 days) by account with optional filters
     *
     * @param accountId the account identifier
     * @param fromBookingDate optional start date filter (max 7 days ago)
     * @param toBookingDate optional end date filter
     * @param creditDebitIndicator optional credit/debit filter
     * @param page page number (0-based)
     * @param size page size
     * @return list of recent transactions
     */
    List<Transaction> findRecentByAccountId(AccountId accountId,
                                            Optional<LocalDate> fromBookingDate,
                                            Optional<LocalDate> toBookingDate,
                                            Optional<CreditDebitIndicator> creditDebitIndicator,
                                            int page,
                                            int size);

    /**
     * Counts transactions by account with optional filters
     *
     * @param accountId the account identifier
     * @param fromBookingDate optional start date filter
     * @param toBookingDate optional end date filter
     * @param creditDebitIndicator optional credit/debit filter
     * @return total count of transactions
     */
    long countByAccountId(AccountId accountId,
                          Optional<LocalDate> fromBookingDate,
                          Optional<LocalDate> toBookingDate,
                          Optional<CreditDebitIndicator> creditDebitIndicator);

    /**
     * Checks if a transaction exists
     *
     * @param transactionId the transaction identifier
     * @return true if transaction exists
     */
    boolean existsById(TransactionId transactionId);
}
