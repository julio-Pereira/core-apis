package com.openfinance.core.domain.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Value object representing a unique transaction identifier
 */
public class TransactionId {

    @NotBlank(message = "Transaction ID cannot be blank")
    @Size(min = 1, max = 100, message = "Transaction ID must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,99}$", message = "Invalid transaction ID format")
    private final String value;

    private TransactionId(String value) {
        this.value = value;
    }

    public static TransactionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        return new TransactionId(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionId that = (TransactionId) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}