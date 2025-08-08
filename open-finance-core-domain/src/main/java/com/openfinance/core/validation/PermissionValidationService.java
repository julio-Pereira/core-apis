package com.openfinance.core.validation;

import com.openfinance.core.exceptions.BusinessRuleViolationException;

/**
 * Domain service for permission validation business rules
 */
public class PermissionValidationService {

    // Open Finance API permissions
    public static final String ACCOUNTS_READ = "ACCOUNTS_READ";
    public static final String ACCOUNTS_BALANCES_READ = "ACCOUNTS_BALANCES_READ";
    public static final String ACCOUNTS_TRANSACTIONS_READ = "ACCOUNTS_TRANSACTIONS_READ";
    public static final String ACCOUNTS_OVERDRAFT_LIMITS_READ = "ACCOUNTS_OVERDRAFT_LIMITS_READ";

    /**
     * Validates that the required permission is present
     *
     * @param hasPermission whether the permission is granted
     * @param requiredPermission the required permission
     * @throws BusinessRuleViolationException if permission is not granted
     */
    public void validatePermission(boolean hasPermission, String requiredPermission) {
        if (!hasPermission) {
            throw new BusinessRuleViolationException(
                    "Required permission not granted: " + requiredPermission
            );
        }
    }

    /**
     * Validates accounts read permission
     *
     * @param hasPermission whether the permission is granted
     */
    public void validateAccountsReadPermission(boolean hasPermission) {
        validatePermission(hasPermission, ACCOUNTS_READ);
    }

    /**
     * Validates accounts balances read permission
     *
     * @param hasPermission whether the permission is granted
     */
    public void validateAccountsBalancesReadPermission(boolean hasPermission) {
        validatePermission(hasPermission, ACCOUNTS_BALANCES_READ);
    }

    /**
     * Validates accounts transactions read permission
     *
     * @param hasPermission whether the permission is granted
     */
    public void validateAccountsTransactionsReadPermission(boolean hasPermission) {
        validatePermission(hasPermission, ACCOUNTS_TRANSACTIONS_READ);
    }

    /**
     * Validates accounts overdraft limits read permission
     *
     * @param hasPermission whether the permission is granted
     */
    public void validateAccountsOverdraftLimitsReadPermission(boolean hasPermission) {
        validatePermission(hasPermission, ACCOUNTS_OVERDRAFT_LIMITS_READ);
    }
}