package com.openfinance.usecase.utils;

public enum FrequencyCategory {
    HIGH("High Frequency", 1500),
    MEDIUM_HIGH("Medium-High Frequency", 1500),
    MEDIUM("Medium Frequency", 2000),
    LOW("Low Frequency", 4000);

    private final String description;
    private final int baseTPM;

    FrequencyCategory(String description, int baseTPM) {
        this.description = description;
        this.baseTPM = baseTPM;
    }

    public String getDescription() { return description; }
    public int getBaseTPM() { return baseTPM; }
}