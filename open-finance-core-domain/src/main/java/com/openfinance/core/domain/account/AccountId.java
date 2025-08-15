package com.openfinance.core.domain.account;

import com.openfinance.core.domain.Identifier;
import com.openfinance.core.utils.IdUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Value object representing a unique account identifier
 */
public class AccountId extends Identifier {

    @NotBlank(message = "Account ID cannot be blank")
    @Size(min = 1, max = 100, message = "Account ID must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,99}$", message = "Invalid account ID format")
    private final String value;

    private AccountId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static AccountId unique() {
        return AccountId.from(IdUtils.uuid());
    }

    public static AccountId from(final String id) { return new AccountId(id); }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountId that = (AccountId) obj;
        return getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}