package com.openfinance.core.validation;

import com.openfinance.core.domain.transaction.Transaction;
import com.openfinance.core.enums.CompletedAuthorisedPaymentIndicator;
import com.openfinance.core.enums.TransactionType;
import com.openfinance.core.exceptions.InvalidTransactionStateException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain service for transaction validation business rules
 */
public class TransactionValidationService {

    /**
     * Validates if the transaction can be modified
     *
     * @param transaction the transaction to validate
     * @throws InvalidTransactionStateException if transaction cannot be modified
     */
    public void validateTransactionModification(Transaction transaction) {
        if (transaction.isCompleted()) {
            throw new InvalidTransactionStateException(
                    transaction.getTransactionId(),
                    transaction.getCompletedAuthorisedPaymentType(),
                    "modification"
            );
        }
    }

    /**
     * Validates if the transaction ID should be immutable based on type and date
     *
     * @param transaction the transaction to validate
     * @return true if transaction ID should be immutable
     */
    public boolean shouldTransactionIdBeImmutable(Transaction transaction) {
        TransactionType type = transaction.getType();
        LocalDateTime transactionDateTime = transaction.getTransactionDateTime();
        LocalDate transactionDate = transactionDateTime.toLocalDate();
        LocalDate today = LocalDate.now();

        // For types that require immediate immutability (D0)
        if (type.requiresImmediateImmutability()) {
            return transactionDate.equals(today);
        }

        // For types that allow delayed immutability (D+1)
        if (type.allowsDelayedImmutability()) {
            return transactionDate.isBefore(today);
        }

        // Default case - should be immutable if completed
        return transaction.isCompleted();
    }

    /**
     * Validates transaction date constraints
     *
     * @param transaction the transaction to validate
     */
    public void validateTransactionDate(Transaction transaction) {
        LocalDateTime transactionDateTime = transaction.getTransactionDateTime();
        LocalDateTime now = LocalDateTime.now();

        // Future transactions are allowed but should be marked appropriately
        if (transactionDateTime.isAfter(now) &&
                transaction.getCompletedAuthorisedPaymentType() != CompletedAuthorisedPaymentIndicator.LANCAMENTO_FUTURO) {
            throw new InvalidTransactionStateException(
                    "Future transactions must be marked as LANCAMENTO_FUTURO"
            );
        }

        // Past transactions that are still processing should have a valid reason
        if (transactionDateTime.isBefore(now.minusDays(1)) &&
                transaction.getCompletedAuthorisedPaymentType() == CompletedAuthorisedPaymentIndicator.TRANSACAO_PROCESSANDO) {
            throw new InvalidTransactionStateException(
                    "Transactions older than 1 day should not be in TRANSACAO_PROCESSANDO state"
            );
        }
    }

    /**
     * Validates if the transaction requires counterparty information
     *
     * @param transaction the transaction to validate
     */
    public void validateCounterpartyRequirements(Transaction transaction) {
        TransactionType type = transaction.getType();

        // FOLHA_PAGAMENTO requires counterparty information
        if (type == TransactionType.FOLHA_PAGAMENTO && !transaction.hasPartieInformation()) {
            throw new InvalidTransactionStateException(
                    "FOLHA_PAGAMENTO transactions require counterparty information"
            );
        }
    }
}

