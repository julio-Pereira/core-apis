package com.openfinance.core.events.transaction;

import com.openfinance.core.domain.transaction.CreditDebitIndicator;
import com.openfinance.core.events.DomainEvent;
import com.openfinance.core.domain.account.AccountId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain event fired when account transactions are requested
 */
public class TransactionsRequestedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final AccountId accountId;
    private final String consentId;
    private final String organizationId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final CreditDebitIndicator creditDebitIndicator;
    private final int page;
    private final int size;
    private final boolean isRecentTransactions;

    public TransactionsRequestedEvent(AccountId accountId,
                                      String consentId,
                                      String organizationId,
                                      Optional<LocalDate> fromDate,
                                      Optional<LocalDate> toDate,
                                      Optional<CreditDebitIndicator> creditDebitIndicator,
                                      int page,
                                      int size,
                                      boolean isRecentTransactions) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.accountId = accountId;
        this.consentId = consentId;
        this.organizationId = organizationId;
        this.fromDate = fromDate.orElse(null);
        this.toDate = toDate.orElse(null);
        this.creditDebitIndicator = creditDebitIndicator.orElse(null);
        this.page = page;
        this.size = size;
        this.isRecentTransactions = isRecentTransactions;
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
        return isRecentTransactions ? "RecentTransactionsRequested" : "TransactionsRequested";
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

    public Optional<LocalDate> getFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public Optional<LocalDate> getToDate() {
        return Optional.ofNullable(toDate);
    }

    public Optional<CreditDebitIndicator> getCreditDebitIndicator() {
        return Optional.ofNullable(creditDebitIndicator);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public boolean isRecentTransactions() {
        return isRecentTransactions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionsRequestedEvent that = (TransactionsRequestedEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "TransactionsRequestedEvent{" +
                "eventId='" + eventId + '\'' +
                ", accountId=" + accountId +
                ", isRecentTransactions=" + isRecentTransactions +
                ", page=" + page +
                ", size=" + size +
                ", occurredOn=" + occurredOn +
                '}';
    }
}