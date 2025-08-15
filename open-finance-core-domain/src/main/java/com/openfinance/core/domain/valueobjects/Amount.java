package com.openfinance.core.domain.valueobjects;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing a monetary amount with currency
 */
public class Amount {

    @NotNull(message = "Amount value cannot be null")
    private final BigDecimal value;

    @NotNull(message = "Currency cannot be null")
    private final Currency currency;

    private Amount(BigDecimal value, Currency currency) {
        this.value = value.setScale(4, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Amount of(BigDecimal value, Currency currency) {
        if (value == null) {
            throw new IllegalArgumentException("Amount value cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return new Amount(value, currency);
    }

    public static Amount of(String value, Currency currency) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount value cannot be null or empty");
        }
        try {
            return of(new BigDecimal(value), currency);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + value, e);
        }
    }

    public static Amount zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Amount add(Amount other) {
        validateSameCurrency(other);
        return new Amount(this.value.add(other.value), this.currency);
    }

    public Amount subtract(Amount other) {
        validateSameCurrency(other);
        return new Amount(this.value.subtract(other.value), this.currency);
    }

    public Amount multiply(BigDecimal multiplier) {
        return new Amount(this.value.multiply(multiplier), this.currency);
    }

    public Amount negate() {
        return new Amount(this.value.negate(), this.currency);
    }

    public boolean isGreaterThan(Amount other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isGreaterThanOrEqual(Amount other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) >= 0;
    }

    public boolean isLessThan(Amount other) {
        validateSameCurrency(other);
        return this.value.compareTo(other.value) < 0;
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }

    private void validateSameCurrency(Amount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on amounts with different currencies");
        }
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Amount amount = (Amount) obj;
        return Objects.equals(value, amount.value) && Objects.equals(currency, amount.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, currency);
    }

    @Override
    public String toString() {
        return value + " " + currency;
    }
}
