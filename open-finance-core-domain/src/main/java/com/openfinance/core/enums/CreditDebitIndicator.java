package com.openfinance.core.enums;

/**
 * Enum indicating the type of transaction entry (credit or debit)
 */
public enum CreditDebitIndicator {

    /**
     * Credit entry - money coming into the account
     */
    CREDITO("Crédito"),

    /**
     * Debit entry - money going out of the account
     */
    DEBITO("Débito");

    private final String description;

    CreditDebitIndicator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
