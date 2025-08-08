package com.openfinance.core.enums;

/**
 * Enum representing the subtypes of accounts based on ownership
 */
public enum AccountSubType {

    INDIVIDUAL("Individual"),

    CONJUNTA_SIMPLES("Conjunta Simples"),

    CONJUNTA_SOLIDARIA("Conjunta Solidária");

    private final String description;

    AccountSubType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}