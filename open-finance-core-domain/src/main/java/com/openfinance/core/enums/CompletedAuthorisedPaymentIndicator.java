package com.openfinance.core.enums;

/**
 * Enum indicating the transaction status
 */
public enum CompletedAuthorisedPaymentIndicator {

    /**
     * Transaction completed - transactionId becomes immutable
     */
    TRANSACAO_EFETIVADA("Transação Efetivada"),

    /**
     * Future transaction - will be completed in the future, transactionId may change
     */
    LANCAMENTO_FUTURO("Lançamento Futuro"),

    /**
     * Transaction processing - still being processed, transactionId may change
     */
    TRANSACAO_PROCESSANDO("Transação Processando");

    private final String description;

    CompletedAuthorisedPaymentIndicator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

