package com.openfinance.core.validation;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountBalance;
import com.openfinance.core.domain.transaction.OverdraftLimits;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.exceptions.AccountNotFoundException;
import com.openfinance.core.exceptions.InvalidAccountTypeException;
import com.openfinance.core.valueobjects.AccountId;

/**
 * Domain service encapsulating complex account business rules
 */
public class AccountBusinessService {

    private final AccountValidationService accountValidationService;
    private final BalanceCalculationService balanceCalculationService;

    public AccountBusinessService(AccountValidationService accountValidationService,
                                  BalanceCalculationService balanceCalculationService) {
        this.accountValidationService = accountValidationService;
        this.balanceCalculationService = balanceCalculationService;
    }

    /**
     * Validates account for balance operations
     *
     * @param account the account to validate
     */
    public void validateForBalanceOperations(Account account) {
        if (account == null) {
            throw new AccountNotFoundException("Account cannot be null");
        }
        accountValidationService.validateTransactionSupport(account);
    }

    /**
     * Validates account for overdraft operations
     *
     * @param account the account to validate
     */
    public void validateForOverdraftOperations(Account account) {
        if (account == null) {
            throw new AccountNotFoundException("Account cannot be null");
        }
        accountValidationService.validateOverdraftSupport(account);
    }

    /**
     * Determines if an account should have overdraft limits
     *
     * @param account the account to check
     * @return true if account should have overdraft limits
     */
    public boolean shouldHaveOverdraftLimits(Account account) {
        return account.getType() == AccountType.CONTA_DEPOSITO_A_VISTA;
    }

    /**
     * Creates empty overdraft limits for accounts that don't support overdraft
     *
     * @param accountId the account identifier
     * @return empty overdraft limits
     */
    public OverdraftLimits createEmptyOverdraftLimits(AccountId accountId) {
        return OverdraftLimits.empty(accountId);
    }

    /**
     * Validates account consistency with balance and overdraft data
     *
     * @param account the account
     * @param accountBalance the account balance (optional)
     * @param overdraftLimits the overdraft limits (optional)
     */
    public void validateAccountConsistency(Account account,
                                           AccountBalance accountBalance,
                                           OverdraftLimits overdraftLimits) {
        // Validate account and balance belong to the same account
        if (accountBalance != null && !account.getAccountId().equals(accountBalance.getAccountId())) {
            throw new InvalidAccountTypeException("Account and balance do not match");
        }

        // Validate account and overdraft limits belong to the same account
        if (overdraftLimits != null && !account.getAccountId().equals(overdraftLimits.getAccountId())) {
            throw new InvalidAccountTypeException("Account and overdraft limits do not match");
        }

        // Validate that non-checking accounts don't have overdraft limits
        if (!shouldHaveOverdraftLimits(account) && overdraftLimits != null && overdraftLimits.hasAnyOverdraftLimit()) {
            throw new InvalidAccountTypeException(account.getType(), "overdraft limits");
        }
    }
}
