package com.openfinance.core.domain.account;

/**
 * Enum representing the type of person involved in the transaction
 */
public enum PartiePersonType {

    /**
     * Natural person - individual (CPF)
     */
    PESSOA_NATURAL("Pessoa Natural"),

    /**
     * Legal person - company (CNPJ)
     */
    PESSOA_JURIDICA("Pessoa Jurídica");

    private final String description;

    PartiePersonType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}