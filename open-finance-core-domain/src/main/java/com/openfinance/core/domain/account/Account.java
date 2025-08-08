package com.openfinance.core.domain.account;

import com.openfinance.core.enums.AccountSubType;
import com.openfinance.core.enums.AccountType;
import com.openfinance.core.valueobjects.AccountId;
import com.openfinance.core.valueobjects.BranchCode;
import com.openfinance.core.valueobjects.CompeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Currency;
import java.util.Objects;

/**
 * Core domain entity representing a financial account in the Open Finance ecosystem.
 * This entity encapsulates all account-related business logic and invariants.
 */
public class Account {

    @NotBlank(message = "Brand name cannot be blank")
    @Size(max = 80, message = "Brand name must not exceed 80 characters")
    @Pattern(regexp = "[\\w\\W\\s]*", message = "Invalid brand name format")
    private final String brandName;

    @NotBlank(message = "Company CNPJ cannot be blank")
    @Pattern(regexp = "^\\d{14}$", message = "Company CNPJ must contain exactly 14 digits")
    private final String companyCnpj;

    @NotNull(message = "Account type cannot be null")
    private final AccountType type;

    @NotNull(message = "Account subtype cannot be null")
    private final AccountSubType subtype;

    @NotNull(message = "COMPE code cannot be null")
    private final CompeCode compeCode;

    private final BranchCode branchCode;

    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "^\\d{8,20}$", message = "Account number must contain 8 to 20 digits")
    private final String number;

    @NotBlank(message = "Check digit cannot be blank")
    @Size(max = 1, message = "Check digit must be exactly 1 character")
    @Pattern(regexp = "[\\w\\W\\s]*", message = "Invalid check digit format")
    private final String checkDigit;

    @NotNull(message = "Account ID cannot be null")
    private final AccountId accountId;

    @NotNull(message = "Currency cannot be null")
    private final Currency currency;

    /**
     * Private constructor to ensure immutability and controlled creation
     */
    private Account(Builder builder) {
        this.brandName = builder.brandName;
        this.companyCnpj = builder.companyCnpj;
        this.type = builder.type;
        this.subtype = builder.subtype;
        this.compeCode = builder.compeCode;
        this.branchCode = builder.branchCode;
        this.number = builder.number;
        this.checkDigit = builder.checkDigit;
        this.accountId = builder.accountId;
        this.currency = builder.currency;

        validateBusinessRules();
    }

    /**
     * Validates business rules for account creation
     */
    private void validateBusinessRules() {
        // Branch code is mandatory for accounts different from CONTA_PAGAMENTO_PRE_PAGA
        if (type != AccountType.CONTA_PAGAMENTO_PRE_PAGA && branchCode == null) {
            throw new IllegalArgumentException("Branch code is mandatory for account type: " + type);
        }

        // Pre-paid accounts should not have branch code
        if (type == AccountType.CONTA_PAGAMENTO_PRE_PAGA && branchCode != null) {
            throw new IllegalArgumentException("Pre-paid accounts should not have branch code");
        }
    }

    /**
     * Creates a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getBrandName() {
        return brandName;
    }

    public String getCompanyCnpj() {
        return companyCnpj;
    }

    public AccountType getType() {
        return type;
    }

    public AccountSubType getSubtype() {
        return subtype;
    }

    public CompeCode getCompeCode() {
        return compeCode;
    }

    public BranchCode getBranchCode() {
        return branchCode;
    }

    public String getNumber() {
        return number;
    }

    public String getCheckDigit() {
        return checkDigit;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Checks if this account is a pre-paid account
     */
    public boolean isPrePaidAccount() {
        return AccountType.CONTA_PAGAMENTO_PRE_PAGA.equals(this.type);
    }

    /**
     * Checks if this account is a savings account
     */
    public boolean isSavingsAccount() {
        return AccountType.CONTA_POUPANCA.equals(this.type);
    }

    /**
     * Checks if this account is a checking account
     */
    public boolean isCheckingAccount() {
        return AccountType.CONTA_DEPOSITO_A_VISTA.equals(this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Account account = (Account) obj;
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", type=" + type +
                ", brandName='" + brandName + '\'' +
                '}';
    }

    /**
     * Builder pattern for Account creation
     */
    public static class Builder {
        private String brandName;
        private String companyCnpj;
        private AccountType type;
        private AccountSubType subtype;
        private CompeCode compeCode;
        private BranchCode branchCode;
        private String number;
        private String checkDigit;
        private AccountId accountId;
        private Currency currency;

        public Builder brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder companyCnpj(String companyCnpj) {
            this.companyCnpj = companyCnpj;
            return this;
        }

        public Builder type(AccountType type) {
            this.type = type;
            return this;
        }

        public Builder subtype(AccountSubType subtype) {
            this.subtype = subtype;
            return this;
        }

        public Builder compeCode(CompeCode compeCode) {
            this.compeCode = compeCode;
            return this;
        }

        public Builder branchCode(BranchCode branchCode) {
            this.branchCode = branchCode;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder checkDigit(String checkDigit) {
            this.checkDigit = checkDigit;
            return this;
        }

        public Builder accountId(AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder currency(Currency currency) {
            this.currency = currency;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}