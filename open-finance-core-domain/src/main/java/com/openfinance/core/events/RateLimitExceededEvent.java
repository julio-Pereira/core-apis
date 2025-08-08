package com.openfinance.core.events;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event fired when rate limit is exceeded
 */
public class RateLimitExceededEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String identifier;
    private final String endpoint;
    private final String limitType;
    private final long currentCount;
    private final long limit;

    public RateLimitExceededEvent(String identifier,
                                  String endpoint,
                                  String limitType,
                                  long currentCount,
                                  long limit) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.identifier = identifier;
        this.endpoint = endpoint;
        this.limitType = limitType;
        this.currentCount = currentCount;
        this.limit = limit;
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
        return "RateLimitExceeded";
    }

    @Override
    public String getAggregateId() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getLimitType() {
        return limitType;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public long getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RateLimitExceededEvent that = (RateLimitExceededEvent) obj;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "RateLimitExceededEvent{" +
                "eventId='" + eventId + '\'' +
                ", identifier='" + identifier + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", limitType='" + limitType + '\'' +
                ", currentCount=" + currentCount +
                ", limit=" + limit +
                ", occurredOn=" + occurredOn +
                '}';
    }
}