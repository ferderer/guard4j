package de.ferderer.guard4j.classification;

/**
 * Unified level enum for errors, events, and logging.
 *
 * <p>This enum serves multiple purposes:
 * <ul>
 *   <li><strong>Error Severity</strong> - How severe an error is</li>
 *   <li><strong>Event Log Level</strong> - How to log business events</li>
 *   <li><strong>Alerting</strong> - What priority alerts should have</li>
 * </ul>
 *
 * <p>Levels are ordered from least to most severe, enabling comparison
 * operations for filtering and threshold checking.
 *
 * @since 1.0.0
 */
public enum Level {

    /**
     * Trace level - very detailed diagnostic information.
     * Typically only enabled in development for debugging specific issues.
     * No alerting recommended.
     */
    TRACE,

    /**
     * Debug level - detailed diagnostic information.
     * Useful for debugging and detailed troubleshooting.
     * No alerting recommended.
     */
    DEBUG,

    /**
     * Info level - general information about application flow.
     * Standard level for business events and normal operations.
     * Low priority alerting if any.
     */
    INFO,

    /**
     * Warn level - potentially harmful situations.
     * Used for recoverable errors and unexpected conditions.
     * Medium priority alerting - investigate during business hours.
     */
    WARN,

    /**
     * Error level - error events that don't stop the application.
     * Used for most application errors and exceptions.
     * High priority alerting - immediate attention during business hours.
     */
    ERROR,

    /**
     * Fatal level - very severe error events that may cause the application to abort.
     * Reserved for critical system failures.
     * Critical alerting - immediate 24/7 attention required.
     */
    FATAL;

    /**
     * Returns whether this level indicates an error condition.
     *
     * @return true for ERROR and FATAL levels
     */
    public boolean isError() {
        return this == ERROR || this == FATAL;
    }

    /**
     * Returns whether this level should be included in production logs.
     *
     * @return true for INFO, WARN, ERROR, and FATAL levels
     */
    public boolean isProductionLevel() {
        return ordinal() >= INFO.ordinal();
    }

    /**
     * Returns whether this level is more severe than the specified level.
     *
     * @param other the level to compare against
     * @return true if this level is more severe
     */
    public boolean isMoreSevereThan(Level other) {
        return ordinal() > other.ordinal();
    }

    /**
     * Returns whether this level is at least as severe as the specified level.
     *
     * @param other the level to compare against
     * @return true if this level is at least as severe
     */
    public boolean isAtLeast(Level other) {
        return ordinal() >= other.ordinal();
    }
}
