package com.openfinance.core.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private final String value;

    private Email(String value) {
        this.value = Objects.requireNonNull(value, "Email cannot be null");
        validate(value);
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private void validate(String email) {
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return value.substring(value.indexOf("@") + 1);
    }

    public String getLocalPart() {
        return value.substring(0, value.indexOf("@"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value.toLowerCase(), email.value.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}