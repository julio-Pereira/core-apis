package com.openfinance.core.validation;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.exceptions.InvalidAccountTypeException;

/**
 *  Domain service for account validation business rules
 */
public class AccountValidationService {

    /**
     * Validates if the account supports overdraft operations
     *
     * @param account the account to validate
     * @throws InvalidAccountTypeException if account type doesn't support overdraft
     */
    public void validateOverdraftSupport(Account account) {
        if (!supportsOverdraft(account.getType())) {
            throw new InvalidAccountTypeException(account.getType(), "overdraft operations");
        }
    }

    /**
     * Validates if the account can have transactions
     *
     * @param account the account to validate
     * @throws InvalidAccountTypeException if account type doesn't support transactions
     */
    public void validateTransactionSupport(Account account) {
        if (!supportsTransactions(account.getType())) {
            throw new InvalidAccountTypeException(account.getType(), "transaction operations");
        }
    }

    /**
     * Validates if the account requires branch code
     *
     * @param account the account to validate
     * @throws InvalidAccountTypeException if branch code requirements are not met
     */
    public void validateBranchCodeRequirement(Account account) {
        if (requiresBranchCode(account.getType()) && account.getBranchCode() == null) {
            throw new InvalidAccountTypeException("Branch code is required for account type: " + account.getType());
        }

        if (!requiresBranchCode(account.getType()) && account.getBranchCode() != null) {
            throw new InvalidAccountTypeException("Branch code should not be present for account type: " + account.getType());
        }
    }

    /**
     * Checks if the account type supports overdraft operations
     *
     * @param accountType the account type
     * @return true if supports overdraft
     */
    private boolean supportsOverdraft(AccountType accountType) {
        return accountType == AccountType.CONTA_DEPOSITO_A_VISTA;
    }

    /**
     * Checks if the account type supports transaction operations
     *
     * @param accountType the account type
     * @return true if supports transactions
     */
    private boolean supportsTransactions(AccountType accountType) {
        // All account types support transactions
        return true;
    }

    /**
     * Checks if the account type requires branch code
     *
     * @param accountType the account type
     * @return true if requires branch code
     */
    private boolean requiresBranchCode(AccountType accountType) {
        return accountType != AccountType.CONTA_PAGAMENTO_PRE_PAGA;
    }
}
