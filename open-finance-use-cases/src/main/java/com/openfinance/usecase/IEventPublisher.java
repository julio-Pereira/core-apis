package com.openfinance.usecase;

import com.openfinance.core.events.DomainEvent;

/**
 * Interface for publishing domain events
 * Follows the hexagonal architecture pattern for external communication
 */
public interface IEventPublisher {
    /**
     * Publishes a domain event
     *
     * @param event the domain event to publish
     * @param <T> the type of domain event
     */
    <T extends DomainEvent> void publish(T event);

    /**
     * Publishes multiple domain events
     *
     * @param events the domain events to publish
     * @param <T> the type of domain events
     */
    <T extends DomainEvent> void publishAll(Iterable<T> events);
}
