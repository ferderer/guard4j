package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;
import java.time.Instant;

/**
 * Base interface for all events that generate observability data.
 *
 * <p>ObservableEvent provides a unified contract for events that can be
 * processed by observability systems. All events must provide their type,
 * logging level, and metrics configuration.
 *
 * <p>Context information (user ID, request details, etc.) is handled by
 * the {@link ObservabilityProcessor} implementations, not by the events themselves.
 *
 * @since 1.0.0
 */
public interface ObservableEvent {

    /**
     * Returns the event type identifier.
     * This uniquely identifies what kind of event this is.
     *
     * @return the event type identifier, never null
     */
    String eventType();

    /**
     * Returns the level for logging and alerting.
     * This determines how the event should be logged and monitored.
     *
     * @return the level for this event, never null
     */
    Level level();

    /**
     * Returns whether this event should generate metrics.
     * Controls whether the event is sent to metrics collection systems.
     *
     * @return true if metrics should be collected for this event
     */
    boolean hasMetrics();

    /**
     * Returns when this event occurred.
     * Default implementation returns the current time.
     *
     * @return the event timestamp, never null
     */
    default Instant timestamp() {
        return Instant.now();
    }
}
