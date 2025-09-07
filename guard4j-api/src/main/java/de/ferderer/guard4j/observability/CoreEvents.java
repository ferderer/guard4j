package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;

/**
 * Core events that Guard4j automatically captures.
 *
 * <p>These events represent Guard4j's own operational behavior that gets
 * automatically logged and tracked. Applications should define their own
 * business events by implementing {@link EventConfig}.
 *
 * @since 1.0.0
 */
public enum CoreEvents implements EventConfig {

    /**
     * Guard4j framework integration has been initialized.
     * Logged once during application startup.
     */
    GUARD4J_INITIALIZED(Level.INFO, true),

    /**
     * HTTP request has been processed through Guard4j.
     * Logged for every request that goes through Guard4j error handling.
     * Includes timing and outcome information.
     */
    HTTP_REQUEST_PROCESSED(Level.INFO, true);

    private final Level level;
    private final boolean hasMetrics;

    CoreEvents(Level level, boolean hasMetrics) {
        this.level = level;
        this.hasMetrics = hasMetrics;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public boolean hasMetrics() {
        return hasMetrics;
    }
}
