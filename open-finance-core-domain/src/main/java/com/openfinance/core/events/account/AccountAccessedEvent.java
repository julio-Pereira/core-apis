package com.openfinance.core.events.account;

import com.openfinance.core.events.DomainEvent;
import com.openfinance.core.domain.account.AccountId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event fired when an account is accessed
 */
public class AccountAccessedEvent implements DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final AccountId accountId;
    private final String consentId;
    private final String operation;
    private final String organizationId;
    
    public AccountAccessedEvent(AccountId accountId, String consentId, String operation, String organizationId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.accountId = accountId;
        this.consentId = consentId;
        this.operation = operation;
        this.organizationId = organizationId;
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public String getEventType() {
        return "AccountAccessed";
    }
    
    @Override
    public String getAggregateId() {
        return accountId.getValue();
    }
    
    public AccountId getAccountId() {
        return accountId;
    }
    
    public String getConsentId() {
        return consentId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountAccessedEvent that = (AccountAccessedEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
    
    @Override
    public String toString() {
        return "AccountAccessedEvent{" +
                "eventId='" + eventId + '\'' +
                ", accountId=" + accountId +
                ", operation='" + operation + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}