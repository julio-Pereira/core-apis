package com.openfinance.core.valueobjects;

import java.util.Objects;

public class CPF {
    private final String value;

    private CPF(String value) {
        this.value = Objects.requireNonNull(value, "CPF cannot be null");
        validate(value);
    }

    public static CPF of(String value) {
        return new CPF(value);
    }

    private void validate(String cpf) {
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        
        if (cleanCpf.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits");
        }
        
        if (isAllSameDigit(cleanCpf)) {
            throw new IllegalArgumentException("Invalid CPF: all digits are the same");
        }
        
        if (!isValidCpf(cleanCpf)) {
            throw new IllegalArgumentException("Invalid CPF: " + cpf);
        }
    }

    private boolean isAllSameDigit(String cpf) {
        char firstDigit = cpf.charAt(0);
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != firstDigit) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidCpf(String cpf) {
        int firstDigit = calculateDigit(cpf, 10);
        int secondDigit = calculateDigit(cpf, 11);
        
        return firstDigit == Character.getNumericValue(cpf.charAt(9)) &&
               secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private int calculateDigit(String cpf, int weight) {
        int sum = 0;
        for (int i = 0; i < weight - 1; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    public String getValue() {
        return value;
    }

    public String getFormattedValue() {
        String cleanCpf = value.replaceAll("[^0-9]", "");
        return cleanCpf.substring(0, 3) + "." + 
               cleanCpf.substring(3, 6) + "." + 
               cleanCpf.substring(6, 9) + "-" + 
               cleanCpf.substring(9, 11);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPF cpf = (CPF) o;
        return Objects.equals(value.replaceAll("[^0-9]", ""), 
                            cpf.value.replaceAll("[^0-9]", ""));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.replaceAll("[^0-9]", ""));
    }

    @Override
    public String toString() {
        return getFormattedValue();
    }
}