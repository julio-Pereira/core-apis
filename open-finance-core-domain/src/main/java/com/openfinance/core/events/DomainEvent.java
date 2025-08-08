package com.openfinance.core.events;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events in the Open Finance core domain
 */
public interface DomainEvent {

    String getEventId();

    LocalDateTime getOccurredOn();

    String getEventType();

    String getAggregateId();

    default String getVersion() {
        return "1.0";
    }
}
