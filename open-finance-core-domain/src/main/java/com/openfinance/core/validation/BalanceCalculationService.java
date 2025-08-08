package com.openfinance.core.validation;

import com.openfinance.core.domain.account.AccountBalance;
import com.openfinance.core.domain.transaction.OverdraftLimits;
import com.openfinance.core.domain.transaction.Transaction;
import com.openfinance.core.valueobjects.Amount;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

/**
 * Domain service for balance calculation business rules
 */
public class BalanceCalculationService {

    /**
     * Calculates the effective balance considering overdraft limits
     *
     * @param accountBalance the account balance
     * @param overdraftLimits the overdraft limits (optional)
     * @return effective balance including overdraft
     */
    public Amount calculateEffectiveBalance(AccountBalance accountBalance,
                                            Optional<OverdraftLimits> overdraftLimits) {
        Amount baseBalance = accountBalance.getEffectiveAvailableBalance();

        if (overdraftLimits.isPresent()) {
            Optional<Amount> availableOverdraft = overdraftLimits.get().getAvailableOverdraftLimit();
            if (availableOverdraft.isPresent()) {
                return baseBalance.add(availableOverdraft.get());
            }
        }

        return baseBalance;
    }

    /**
     * Calculates projected balance after applying a list of pending transactions
     *
     * @param currentBalance the current account balance
     * @param pendingTransactions list of pending transactions
     * @return projected balance
     */
    public Amount calculateProjectedBalance(AccountBalance currentBalance,
                                            List<Transaction> pendingTransactions) {
        Amount projectedBalance = currentBalance.getEffectiveAvailableBalance();

        for (Transaction transaction : pendingTransactions) {
            if (!transaction.isCompleted()) {
                Amount effectiveAmount = transaction.getEffectiveAmount();
                projectedBalance = projectedBalance.add(effectiveAmount);
            }
        }

        return projectedBalance;
    }

    /**
     * Checks if a transaction would cause the account to exceed overdraft limits
     *
     * @param accountBalance current account balance
     * @param overdraftLimits overdraft limits (optional)
     * @param transactionAmount the transaction amount (negative for debits)
     * @return true if transaction would exceed limits
     */
    public boolean wouldExceedOverdraftLimits(AccountBalance accountBalance,
                                              Optional<OverdraftLimits> overdraftLimits,
                                              Amount transactionAmount) {
        Amount effectiveBalance = calculateEffectiveBalance(accountBalance, overdraftLimits);
        Amount balanceAfterTransaction = effectiveBalance.add(transactionAmount);

        // If no overdraft limits, check only available balance
        if (overdraftLimits.isEmpty()) {
            return balanceAfterTransaction.isNegative();
        }

        // With overdraft limits, check if it would exceed the total limit
        OverdraftLimits limits = overdraftLimits.get();
        Optional<Amount> contractedLimit = limits.getOverdraftContractedLimit();

        if (contractedLimit.isEmpty()) {
            return balanceAfterTransaction.isNegative();
        }

        // Calculate maximum allowed negative balance (overdraft limit)
        Amount maxNegativeBalance = contractedLimit.get().negate();
        return balanceAfterTransaction.isLessThan(maxNegativeBalance);
    }

    /**
     * Calculates the total exposure (used overdraft + unarranged overdraft)
     *
     * @param overdraftLimits the overdraft limits
     * @return total exposure amount
     */
    public Amount calculateTotalExposure(OverdraftLimits overdraftLimits) {
        Currency currency = Currency.getInstance("BRL"); // Default currency
        Amount totalExposure = Amount.zero(currency);

        // Add used overdraft limit
        Optional<Amount> usedLimit = overdraftLimits.getOverdraftUsedLimit();
        if (usedLimit.isPresent()) {
            totalExposure = totalExposure.add(usedLimit.get());
            currency = usedLimit.get().getCurrency();
        }

        // Add unarranged overdraft amount
        Optional<Amount> unarrangedAmount = overdraftLimits.getUnarrangedOverdraftAmount();
        if (unarrangedAmount.isPresent()) {
            if (totalExposure.isZero()) {
                currency = unarrangedAmount.get().getCurrency();
                totalExposure = Amount.zero(currency);
            }
            totalExposure = totalExposure.add(unarrangedAmount.get());
        }

        return totalExposure;
    }
}
