package com.openfinance.core.domain.transaction;

import com.openfinance.core.domain.account.AccountId;
import com.openfinance.core.domain.valueobjects.Amount;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Core domain entity representing overdraft limits for checking accounts in the Open Finance ecosystem.
 * This entity encapsulates overdraft-related business logic and constraints.
 */
public class OverdraftLimits {

    @NotNull(message = "Account ID cannot be null")
    private final AccountId accountId;

    // Optional - Overdraft contracted limit (limite contratado do cheque especial)
    private final Amount overdraftContractedLimit;

    // Optional - Overdraft used limit (valor utilizado do limite do cheque especial)
    private final Amount overdraftUsedLimit;

    // Optional - Unarranged overdraft amount (operação contratada em caráter emergencial)
    private final Amount unarrangedOverdraftAmount;

    /**
     * Private constructor to ensure immutability and controlled creation
     */
    private OverdraftLimits(Builder builder) {
        this.accountId = builder.accountId;
        this.overdraftContractedLimit = builder.overdraftContractedLimit;
        this.overdraftUsedLimit = builder.overdraftUsedLimit;
        this.unarrangedOverdraftAmount = builder.unarrangedOverdraftAmount;

        validateBusinessRules();
    }

    /**
     * Validates business rules for overdraft limits
     */
    private void validateBusinessRules() {
        // If any overdraft amount is present, ensure currency consistency
        if (hasAnyOverdraftLimit()) {
            validateCurrencyConsistency();
        }

        // Used limit cannot exceed contracted limit
        if (overdraftContractedLimit != null && overdraftUsedLimit != null) {
            if (overdraftUsedLimit.isGreaterThan(overdraftContractedLimit)) {
                throw new IllegalArgumentException("Used overdraft limit cannot exceed contracted limit");
            }
        }

        // All amounts must be non-negative
        validateNonNegativeAmounts();
    }

    /**
     * Validates that all amounts have the same currency
     */
    private void validateCurrencyConsistency() {
        Amount reference = null;

        if (overdraftContractedLimit != null) {
            reference = overdraftContractedLimit;
        } else if (overdraftUsedLimit != null) {
            reference = overdraftUsedLimit;
        } else if (unarrangedOverdraftAmount != null) {
            reference = unarrangedOverdraftAmount;
        }

        if (reference != null) {
            if (overdraftContractedLimit != null && !overdraftContractedLimit.getCurrency().equals(reference.getCurrency())) {
                throw new IllegalArgumentException("All overdraft amounts must have the same currency");
            }
            if (overdraftUsedLimit != null && !overdraftUsedLimit.getCurrency().equals(reference.getCurrency())) {
                throw new IllegalArgumentException("All overdraft amounts must have the same currency");
            }
            if (unarrangedOverdraftAmount != null && !unarrangedOverdraftAmount.getCurrency().equals(reference.getCurrency())) {
                throw new IllegalArgumentException("All overdraft amounts must have the same currency");
            }
        }
    }

    /**
     * Validates that all amounts are non-negative
     */
    private void validateNonNegativeAmounts() {
        if (overdraftContractedLimit != null && overdraftContractedLimit.getValue().signum() < 0) {
            throw new IllegalArgumentException("Overdraft contracted limit cannot be negative");
        }
        if (overdraftUsedLimit != null && overdraftUsedLimit.getValue().signum() < 0) {
            throw new IllegalArgumentException("Overdraft used limit cannot be negative");
        }
        if (unarrangedOverdraftAmount != null && unarrangedOverdraftAmount.getValue().signum() < 0) {
            throw new IllegalArgumentException("Unarranged overdraft amount cannot be negative");
        }
    }

    /**
     * Creates a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an empty overdraft limits instance (no limits configured)
     */
    public static OverdraftLimits empty(AccountId accountId) {
        return builder().accountId(accountId).build();
    }

    /**
     * Checks if the account has any overdraft limits configured
     */
    public boolean hasAnyOverdraftLimit() {
        return overdraftContractedLimit != null ||
                overdraftUsedLimit != null ||
                unarrangedOverdraftAmount != null;
    }

    /**
     * Checks if the account has overdraft contracted limit
     */
    public boolean hasOverdraftContractedLimit() {
        return overdraftContractedLimit != null;
    }

    /**
     * Checks if the account has used overdraft
     */
    public boolean hasUsedOverdraft() {
        return overdraftUsedLimit != null && overdraftUsedLimit.getValue().signum() > 0;
    }

    /**
     * Checks if the account has unarranged overdraft
     */
    public boolean hasUnarrangedOverdraft() {
        return unarrangedOverdraftAmount != null && unarrangedOverdraftAmount.getValue().signum() > 0;
    }

    /**
     * Calculates available overdraft limit
     */
    public Optional<Amount> getAvailableOverdraftLimit() {
        if (overdraftContractedLimit == null) {
            return Optional.empty();
        }

        Amount usedAmount = overdraftUsedLimit != null ? overdraftUsedLimit :
                Amount.zero(overdraftContractedLimit.getCurrency());

        return Optional.of(overdraftContractedLimit.subtract(usedAmount));
    }

    /**
     * Calculates overdraft utilization percentage (0-100)
     */
    public Optional<Double> getOverdraftUtilizationPercentage() {
        if (overdraftContractedLimit == null || overdraftContractedLimit.getValue().signum() == 0) {
            return Optional.empty();
        }

        Amount usedAmount = overdraftUsedLimit != null ? overdraftUsedLimit :
                Amount.zero(overdraftContractedLimit.getCurrency());

        double utilizationRate = usedAmount.getValue()
                .divide(overdraftContractedLimit.getValue(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();

        return Optional.of(Math.min(100.0, Math.max(0.0, utilizationRate)));
    }

    // Getters
    public AccountId getAccountId() {
        return accountId;
    }

    public Optional<Amount> getOverdraftContractedLimit() {
        return Optional.ofNullable(overdraftContractedLimit);
    }

    public Optional<Amount> getOverdraftUsedLimit() {
        return Optional.ofNullable(overdraftUsedLimit);
    }

    public Optional<Amount> getUnarrangedOverdraftAmount() {
        return Optional.ofNullable(unarrangedOverdraftAmount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OverdraftLimits that = (OverdraftLimits) obj;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "OverdraftLimits{" +
                "accountId=" + accountId +
                ", overdraftContractedLimit=" + overdraftContractedLimit +
                ", overdraftUsedLimit=" + overdraftUsedLimit +
                ", unarrangedOverdraftAmount=" + unarrangedOverdraftAmount +
                '}';
    }

    /**
     * Builder pattern for OverdraftLimits creation
     */
    public static class Builder {
        private AccountId accountId;
        private Amount overdraftContractedLimit;
        private Amount overdraftUsedLimit;
        private Amount unarrangedOverdraftAmount;

        public Builder accountId(AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder overdraftContractedLimit(Amount overdraftContractedLimit) {
            this.overdraftContractedLimit = overdraftContractedLimit;
            return this;
        }

        public Builder overdraftUsedLimit(Amount overdraftUsedLimit) {
            this.overdraftUsedLimit = overdraftUsedLimit;
            return this;
        }

        public Builder unarrangedOverdraftAmount(Amount unarrangedOverdraftAmount) {
            this.unarrangedOverdraftAmount = unarrangedOverdraftAmount;
            return this;
        }

        public OverdraftLimits build() {
            return new OverdraftLimits(this);
        }
    }
}