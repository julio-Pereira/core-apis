package com.openfinance.core.domain.transaction;

import com.openfinance.core.enums.CompletedAuthorisedPaymentIndicator;
import com.openfinance.core.enums.CreditDebitIndicator;
import com.openfinance.core.enums.PartiePersonType;
import com.openfinance.core.enums.TransactionType;
import com.openfinance.core.valueobjects.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Core domain entity representing a financial transaction in the Open Finance ecosystem.
 * This entity encapsulates all transaction-related business logic and invariants.
 */
public class Transaction {

    @NotNull(message = "Transaction ID cannot be null")
    private final TransactionId transactionId;

    @NotNull(message = "Account ID cannot be null")
    private final AccountId accountId;

    @NotNull(message = "Completed authorised payment type cannot be null")
    private final CompletedAuthorisedPaymentIndicator completedAuthorisedPaymentType;

    @NotNull(message = "Credit debit type cannot be null")
    private final CreditDebitIndicator creditDebitType;

    @NotBlank(message = "Transaction name cannot be blank")
    @Size(max = 200, message = "Transaction name must not exceed 200 characters")
    @Pattern(regexp = "[\\w\\W\\s]*", message = "Invalid transaction name format")
    private final String transactionName;

    @NotNull(message = "Transaction type cannot be null")
    private final TransactionType type;

    @NotNull(message = "Transaction amount cannot be null")
    private final Amount transactionAmount;

    @NotNull(message = "Transaction date time cannot be null")
    private final LocalDateTime transactionDateTime;

    // Partie information (optional - counterparty details)
    @Pattern(regexp = "^\\d{11}$|^\\d{14}$", message = "Partie CNPJ/CPF must be 11 or 14 digits")
    private final String partieCnpjCpf;

    private final PartiePersonType partiePersonType;
    private final CompeCode partieCompeCode;
    private final BranchCode partieBranchCode;

    @Pattern(regexp = "^\\d{8,20}$", message = "Partie number must contain 8 to 20 digits")
    private final String partieNumber;

    @Size(max = 1, message = "Partie check digit must be exactly 1 character")
    @Pattern(regexp = "[\\w\\W\\s]*", message = "Invalid partie check digit format")
    private final String partieCheckDigit;

    /**
     * Private constructor to ensure immutability and controlled creation
     */
    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.accountId = builder.accountId;
        this.completedAuthorisedPaymentType = builder.completedAuthorisedPaymentType;
        this.creditDebitType = builder.creditDebitType;
        this.transactionName = builder.transactionName;
        this.type = builder.type;
        this.transactionAmount = builder.transactionAmount;
        this.transactionDateTime = builder.transactionDateTime;
        this.partieCnpjCpf = builder.partieCnpjCpf;
        this.partiePersonType = builder.partiePersonType;
        this.partieCompeCode = builder.partieCompeCode;
        this.partieBranchCode = builder.partieBranchCode;
        this.partieNumber = builder.partieNumber;
        this.partieCheckDigit = builder.partieCheckDigit;

        validateBusinessRules();
    }

    /**
     * Validates business rules for transaction creation
     */
    private void validateBusinessRules() {
        // For FOLHA_PAGAMENTO type, partie information validation
        if (type == TransactionType.FOLHA_PAGAMENTO && partieCnpjCpf == null) {
            throw new IllegalArgumentException("Partie CNPJ/CPF is mandatory for FOLHA_PAGAMENTO transactions");
        }

        // Validate partie person type consistency with CNPJ/CPF
        if (partieCnpjCpf != null && partiePersonType != null) {
            if (partieCnpjCpf.length() == 11 && partiePersonType != PartiePersonType.PESSOA_NATURAL) {
                throw new IllegalArgumentException("CPF requires PESSOA_NATURAL person type");
            }
            if (partieCnpjCpf.length() == 14 && partiePersonType != PartiePersonType.PESSOA_JURIDICA) {
                throw new IllegalArgumentException("CNPJ requires PESSOA_JURIDICA person type");
            }
        }

        // Transaction amount must be positive
        if (transactionAmount.getValue().signum() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    /**
     * Creates a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if this transaction is a credit transaction
     */
    public boolean isCredit() {
        return CreditDebitIndicator.CREDITO.equals(this.creditDebitType);
    }

    /**
     * Checks if this transaction is a debit transaction
     */
    public boolean isDebit() {
        return CreditDebitIndicator.DEBITO.equals(this.creditDebitType);
    }

    /**
     * Checks if this transaction is completed/processed
     */
    public boolean isCompleted() {
        return CompletedAuthorisedPaymentIndicator.TRANSACAO_EFETIVADA.equals(this.completedAuthorisedPaymentType);
    }

    /**
     * Checks if this transaction is a future transaction
     */
    public boolean isFutureTransaction() {
        return CompletedAuthorisedPaymentIndicator.LANCAMENTO_FUTURO.equals(this.completedAuthorisedPaymentType);
    }

    /**
     * Checks if this transaction is still processing
     */
    public boolean isProcessing() {
        return CompletedAuthorisedPaymentIndicator.TRANSACAO_PROCESSANDO.equals(this.completedAuthorisedPaymentType);
    }

    /**
     * Checks if this transaction has counterparty information
     */
    public boolean hasPartieInformation() {
        return partieCnpjCpf != null;
    }

    /**
     * Gets the effective amount considering credit/debit indicator
     * Returns positive for credits, negative for debits
     */
    public Amount getEffectiveAmount() {
        return isCredit() ? transactionAmount : transactionAmount.negate();
    }

    // Getters
    public TransactionId getTransactionId() {
        return transactionId;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public CompletedAuthorisedPaymentIndicator getCompletedAuthorisedPaymentType() {
        return completedAuthorisedPaymentType;
    }

    public CreditDebitIndicator getCreditDebitType() {
        return creditDebitType;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public TransactionType getType() {
        return type;
    }

    public Amount getTransactionAmount() {
        return transactionAmount;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public Optional<String> getPartieCnpjCpf() {
        return Optional.ofNullable(partieCnpjCpf);
    }

    public Optional<PartiePersonType> getPartiePersonType() {
        return Optional.ofNullable(partiePersonType);
    }

    public Optional<CompeCode> getPartieCompeCode() {
        return Optional.ofNullable(partieCompeCode);
    }

    public Optional<BranchCode> getPartieBranchCode() {
        return Optional.ofNullable(partieBranchCode);
    }

    public Optional<String> getPartieNumber() {
        return Optional.ofNullable(partieNumber);
    }

    public Optional<String> getPartieCheckDigit() {
        return Optional.ofNullable(partieCheckDigit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", type=" + type +
                ", creditDebitType=" + creditDebitType +
                ", transactionAmount=" + transactionAmount +
                ", transactionDateTime=" + transactionDateTime +
                '}';
    }

    /**
     * Builder pattern for Transaction creation
     */
    public static class Builder {
        private TransactionId transactionId;
        private AccountId accountId;
        private CompletedAuthorisedPaymentIndicator completedAuthorisedPaymentType;
        private CreditDebitIndicator creditDebitType;
        private String transactionName;
        private TransactionType type;
        private Amount transactionAmount;
        private LocalDateTime transactionDateTime;
        private String partieCnpjCpf;
        private PartiePersonType partiePersonType;
        private CompeCode partieCompeCode;
        private BranchCode partieBranchCode;
        private String partieNumber;
        private String partieCheckDigit;

        public Builder transactionId(TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder accountId(AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder completedAuthorisedPaymentType(CompletedAuthorisedPaymentIndicator completedAuthorisedPaymentType) {
            this.completedAuthorisedPaymentType = completedAuthorisedPaymentType;
            return this;
        }

        public Builder creditDebitType(CreditDebitIndicator creditDebitType) {
            this.creditDebitType = creditDebitType;
            return this;
        }

        public Builder transactionName(String transactionName) {
            this.transactionName = transactionName;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder transactionAmount(Amount transactionAmount) {
            this.transactionAmount = transactionAmount;
            return this;
        }

        public Builder transactionDateTime(LocalDateTime transactionDateTime) {
            this.transactionDateTime = transactionDateTime;
            return this;
        }

        public Builder partieCnpjCpf(String partieCnpjCpf) {
            this.partieCnpjCpf = partieCnpjCpf;
            return this;
        }

        public Builder partiePersonType(PartiePersonType partiePersonType) {
            this.partiePersonType = partiePersonType;
            return this;
        }

        public Builder partieCompeCode(CompeCode partieCompeCode) {
            this.partieCompeCode = partieCompeCode;
            return this;
        }

        public Builder partieBranchCode(BranchCode partieBranchCode) {
            this.partieBranchCode = partieBranchCode;
            return this;
        }

        public Builder partieNumber(String partieNumber) {
            this.partieNumber = partieNumber;
            return this;
        }

        public Builder partieCheckDigit(String partieCheckDigit) {
            this.partieCheckDigit = partieCheckDigit;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}