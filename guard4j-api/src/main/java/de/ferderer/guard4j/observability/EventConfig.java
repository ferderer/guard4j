package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;

/**
 * Configuration for observability event behavior.
 * Defines logging and metrics settings for event types.
 *
 * @since 1.0.0
 */
public interface EventConfig {

    /**
     * Returns the event type identifier.
     * For enums, this is typically derived from the enum name.
     *
     * @return the event type identifier
     */
    default String eventType() {
        if (this instanceof Enum<?> enumValue) {
            return enumValue.name().toLowerCase().replace('_', '-');
        }
        return getClass().getSimpleName().toLowerCase();
    }

    /**
     * Returns the log level for this event type.
     *
     * @return the log level
     */
    Level level();

    /**
     * Returns whether this event type should generate metrics.
     *
     * @return true if metrics should be collected for this event type
     */
    boolean hasMetrics();
}
