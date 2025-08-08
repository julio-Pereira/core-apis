package com.openfinance.core.events.account;



import com.openfinance.core.events.DomainEvent;
import com.openfinance.core.valueobjects.AccountId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event fired when account balance is requested
 */
public class AccountBalanceRequestedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final AccountId accountId;
    private final String consentId;
    private final String organizationId;

    public AccountBalanceRequestedEvent(AccountId accountId, String consentId, String organizationId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.accountId = accountId;
        this.consentId = consentId;
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
        return "AccountBalanceRequested";
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

    public String getOrganizationId() {
        return organizationId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountBalanceRequestedEvent that = (AccountBalanceRequestedEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "AccountBalanceRequestedEvent{" +
                "eventId='" + eventId + '\'' +
                ", accountId=" + accountId +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
