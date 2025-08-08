package com.openfinance.core.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * Value object representing a currency code following ISO-4217
 */
public class Currency {

    public static final Currency BRL = new Currency("BRL");
    public static final Currency USD = new Currency("USD");
    public static final Currency EUR = new Currency("EUR");

    @NotBlank(message = "Currency code cannot be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    public static Currency of(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        String normalizedCode = code.trim().toUpperCase();
        if (!normalizedCode.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid currency code format: " + code);
        }
        return new Currency(normalizedCode);
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Currency currency = (Currency) obj;
        return Objects.equals(code, currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
