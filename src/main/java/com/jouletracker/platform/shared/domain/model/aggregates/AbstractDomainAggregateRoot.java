package com.jouletracker.platform.shared.domain.model.aggregates;

import org.jspecify.annotations.NullMarked;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for all domain aggregate roots.
 *
 * <p>Maintains domain events without importing Spring Data, JPA, or any
 * persistence concern. Identity and auditing are intentionally left to the
 * infrastructure layer.</p>
 *
 * <p>All bounded-context domain aggregate roots should extend this class.
 * Persistence-specific behavior belongs to dedicated infrastructure
 * persistence entities.</p>
 *
 * @param <T> the concrete aggregate root type
 */
@NullMarked
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>> {

    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * Registers a domain event to be published after this aggregate is saved.
     *
     * @param event the domain event to register
     */
    protected void registerDomainEvent(Object event) {
        domainEvents.add(event);
    }

    /**
     * Returns all domain events registered on this aggregate since the last publication.
     * Exposed as {@code public} so repository adapters can retrieve and publish them
     * after the aggregate has been persisted.
     *
     * @return the registered domain events
     */
    public Collection<Object> domainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * Clears all registered domain events.
     * Exposed as {@code public} so repository adapters can clear events after publishing.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
