package com.openfinance.core.enums;

/**
 * Enum representing the types of accounts in the Open Finance ecosystem
 */
public enum AccountType {

    CONTA_DEPOSITO_A_VISTA("Conta de Depósito à Vista"),
    CONTA_POUPANCA("Conta Poupança"),
    CONTA_PAGAMENTO_PRE_PAGA("Conta de Pagamento Pré-Paga");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
