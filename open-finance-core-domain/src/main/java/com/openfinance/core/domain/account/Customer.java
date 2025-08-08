package com.openfinance.core.domain.account;

import com.openfinance.core.valueobjects.CustomerId;
import com.openfinance.core.valueobjects.Email;
import com.openfinance.core.valueobjects.CPF;
import java.time.LocalDateTime;
import java.util.Objects;

public class Customer {
    private final CustomerId customerId;
    private String name;
    private final CPF cpf;
    private Email email;
    private String phoneNumber;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    public Customer(CustomerId customerId, String name, CPF cpf, Email email, String phoneNumber) {
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.cpf = Objects.requireNonNull(cpf, "CPF cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        
        validateName(name);
    }

    private void validateName(String name) {
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must have at least 2 characters");
        }
    }

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmail(Email newEmail) {
        this.email = Objects.requireNonNull(newEmail, "Email cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public CPF getCpf() {
        return cpf;
    }

    public Email getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", cpf=" + cpf +
                ", email=" + email +
                ", active=" + active +
                '}';
    }
}