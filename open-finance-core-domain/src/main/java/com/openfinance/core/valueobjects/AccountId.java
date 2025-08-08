package com.openfinance.core.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Value object representing a unique account identifier
 */
public class AccountId {

    @NotBlank(message = "Account ID cannot be blank")
    @Size(min = 1, max = 100, message = "Account ID must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,99}$", message = "Invalid account ID format")
    private final String value;

    private AccountId(String value) {
        this.value = value;
    }

    public static AccountId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        return new AccountId(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountId accountId = (AccountId) obj;
        return Objects.equals(value, accountId.value);
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