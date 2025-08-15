package com.openfinance.core.events;

@FunctionalInterface
public interface IDomainEventPublisher {
    void publishEvent(DomainEvent event);
}
