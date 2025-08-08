package com.openfinance.core.domain.account;

import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.Amount;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Core domain entity representing account balances in the Open Finance ecosystem.
 * Contains available, blocked, and automatically invested amounts.
 */
public class AccountBalance {

    @NotNull(message = "Account ID cannot be null")
    private final AccountId accountId;

    @NotNull(message = "Available amount cannot be null")
    private final Amount availableAmount;

    @NotNull(message = "Blocked amount cannot be null")
    private final Amount blockedAmount;

    @NotNull(message = "Automatically invested amount cannot be null")
    private final Amount automaticallyInvestedAmount;

    @NotNull(message = "Update date time cannot be null")
    private final LocalDateTime updateDateTime;

    /**
     * Private constructor to ensure immutability and controlled creation
     */
    private AccountBalance(Builder builder) {
        this.accountId = builder.accountId;
        this.availableAmount = builder.availableAmount;
        this.blockedAmount = builder.blockedAmount;
        this.automaticallyInvestedAmount = builder.automaticallyInvestedAmount;
        this.updateDateTime = builder.updateDateTime;

        validateBusinessRules();
    }

    /**
     * Validates business rules for account balance
     */
    private void validateBusinessRules() {
        // Ensure all amounts have the same currency
        if (!availableAmount.getCurrency().equals(blockedAmount.getCurrency()) ||
                !availableAmount.getCurrency().equals(automaticallyInvestedAmount.getCurrency())) {
            throw new IllegalArgumentException("All balance amounts must have the same currency");
        }

        // Blocked amount cannot be negative
        if (blockedAmount.getValue().signum() < 0) {
            throw new IllegalArgumentException("Blocked amount cannot be negative");
        }

        // Automatically invested amount cannot be negative
        if (automaticallyInvestedAmount.getValue().signum() < 0) {
            throw new IllegalArgumentException("Automatically invested amount cannot be negative");
        }
    }

    /**
     * Creates a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Calculates the total balance including automatically invested amount
     */
    public Amount getTotalBalance() {
        return availableAmount.add(automaticallyInvestedAmount);
    }

    /**
     * Calculates the effective available balance (available + automatically invested - blocked)
     */
    public Amount getEffectiveAvailableBalance() {
        return getTotalBalance().subtract(blockedAmount);
    }

    /**
     * Checks if the account has sufficient funds for a given amount
     */
    public boolean hasSufficientFunds(Amount requiredAmount) {
        if (!requiredAmount.getCurrency().equals(availableAmount.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch for funds verification");
        }
        return getEffectiveAvailableBalance().isGreaterThanOrEqual(requiredAmount);
    }

    /**
     * Checks if the account has any blocked amount
     */
    public boolean hasBlockedAmount() {
        return blockedAmount.getValue().signum() > 0;
    }

    /**
     * Checks if the account has any automatically invested amount
     */
    public boolean hasAutomaticallyInvestedAmount() {
        return automaticallyInvestedAmount.getValue().signum() > 0;
    }

    // Getters
    public AccountId getAccountId() {
        return accountId;
    }

    public Amount getAvailableAmount() {
        return availableAmount;
    }

    public Amount getBlockedAmount() {
        return blockedAmount;
    }

    public Amount getAutomaticallyInvestedAmount() {
        return automaticallyInvestedAmount;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountBalance that = (AccountBalance) obj;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(updateDateTime, that.updateDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, updateDateTime);
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "accountId=" + accountId +
                ", availableAmount=" + availableAmount +
                ", blockedAmount=" + blockedAmount +
                ", automaticallyInvestedAmount=" + automaticallyInvestedAmount +
                ", updateDateTime=" + updateDateTime +
                '}';
    }

    /**
     * Builder pattern for AccountBalance creation
     */
    public static class Builder {
        private AccountId accountId;
        private Amount availableAmount;
        private Amount blockedAmount;
        private Amount automaticallyInvestedAmount;
        private LocalDateTime updateDateTime;

        public Builder accountId(AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder availableAmount(Amount availableAmount) {
            this.availableAmount = availableAmount;
            return this;
        }

        public Builder blockedAmount(Amount blockedAmount) {
            this.blockedAmount = blockedAmount;
            return this;
        }

        public Builder automaticallyInvestedAmount(Amount automaticallyInvestedAmount) {
            this.automaticallyInvestedAmount = automaticallyInvestedAmount;
            return this;
        }

        public Builder updateDateTime(LocalDateTime updateDateTime) {
            this.updateDateTime = updateDateTime;
            return this;
        }

        public AccountBalance build() {
            return new AccountBalance(this);
        }
    }
}