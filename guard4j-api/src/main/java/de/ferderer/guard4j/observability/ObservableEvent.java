package de.ferderer.guard4j.observability;

import java.time.Instant;

/**
 * Base interface for all events that generate observability data.
 *
 * <p>ObservableEvent provides a simplified contract for events in the new Emitter pattern.
 * Events only need to provide their type and optional metric value, while the logging
 * level is determined by the emitter method used (trace, debug, info, warn, error).
 *
 * <p>Context information (user ID, request details, etc.) is handled by
 * the {@link ObservabilityProcessor} implementations, not by the events themselves.
 *
 * @since 2.0.0
 */
public interface ObservableEvent {

    /**
     * Returns the event type identifier.
     *
     * <p>Default implementation derives the type from the class name,
     * converting from CamelCase to kebab-case (e.g., PaymentProcessedEvent -> payment-processed-event).
     *
     * @return the event type identifier, never null
     */
    default String eventType() {
        return getClass().getSimpleName()
            .replaceAll("([a-z])([A-Z])", "$1-$2")
            .toLowerCase();
    }

    /**
     * Returns the metric value for this event.
     *
     * <p>Default implementation returns 1, which represents a simple event count.
     * Events can override this to provide custom metric values (e.g., payment amounts,
     * processing times, etc.).
     *
     * @return the metric value, typically 1 for simple counting
     */
    default int metric() {
        return 1;
    }

    /**
     * Returns when this event occurred.
     *
     * <p>Default implementation returns the current time.
     *
     * @return the event timestamp, never null
     */
    default Instant timestamp() {
        return Instant.now();
    }
}
