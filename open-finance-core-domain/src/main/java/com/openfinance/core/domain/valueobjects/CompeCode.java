package com.openfinance.core.domain.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * Value object representing a COMPE code (3-digit bank identifier)
 */
public class CompeCode {

    @NotBlank(message = "COMPE code cannot be blank")
    @Pattern(regexp = "^\\d{3}$", message = "COMPE code must be exactly 3 digits")
    private final String value;

    private CompeCode(String value) {
        this.value = value;
    }

    public static CompeCode of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("COMPE code cannot be null or empty");
        }
        String normalizedValue = value.trim();
        if (!normalizedValue.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("Invalid COMPE code format: " + value);
        }
        return new CompeCode(normalizedValue);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompeCode compeCode = (CompeCode) obj;
        return Objects.equals(value, compeCode.value);
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