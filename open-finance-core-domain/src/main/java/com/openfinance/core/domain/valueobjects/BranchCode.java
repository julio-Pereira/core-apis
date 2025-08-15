package com.openfinance.core.domain.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * Value object representing a branch code (4-digit agency identifier)
 */
public class BranchCode {

    @NotBlank(message = "Branch code cannot be blank")
    @Pattern(regexp = "^\\d{4}$", message = "Branch code must be exactly 4 digits")
    private final String value;

    private BranchCode(String value) {
        this.value = value;
    }

    public static BranchCode of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code cannot be null or empty");
        }
        String normalizedValue = value.trim();
        if (!normalizedValue.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Invalid branch code format: " + value);
        }
        return new BranchCode(normalizedValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BranchCode that = (BranchCode) obj;
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
