package de.ferderer.guard4j;

import de.ferderer.guard4j.observability.ObservabilityProcessor;
import de.ferderer.guard4j.observability.ObservableEvent;

/**
 * Main entry point for Guard4j observability.
 *
 * <p>Guard4j provides a unified API for logging both error events and business events
 * across different Java frameworks. This class serves as the primary interface for
 * applications to emit observability events.
 *
 * <h3>Setup</h3>
 * <p>Framework integrations automatically configure the processor during application startup:
 * <pre>{@code
 * // Automatic setup via Spring Boot starter, Quarkus extension, etc.
 * // No manual configuration required
 * }</pre>
 *
 * <h3>Logging Business Events</h3>
 * <pre>{@code
 * // Define your business events
 * public enum MyEvents implements EventConfig {
 *     USER_REGISTERED(LogLevel.INFO, true),
 *     ORDER_PLACED(LogLevel.INFO, true);
 *     // implementation...
 * }
 *
 * // Log events
 * Guard4j.log(new UserRegisteredEvent(userId, userType));
 * Guard4j.log(new OrderPlacedEvent(orderId, amount));
 * }</pre>
 *
 * <h3>Error Events</h3>
 * <p>Error events are automatically captured when {@code AppException} is thrown
 * and handled by framework exception handlers. Manual error logging is typically
 * not required.
 *
 * <h3>Framework Integration</h3>
 * <p>This class is automatically configured by:
 * <ul>
 *   <li>Spring Boot: {@code guard4j-spring-boot-starter}</li>
 *   <li>Quarkus: {@code guard4j-quarkus}</li>
 *   <li>Micronaut: {@code guard4j-micronaut}</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class Guard4j {

    private static ObservabilityProcessor processor;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Guard4j() {}

    /**
     * Sets the observability processor for handling events.
     *
     * <p>This method is typically called automatically by framework integrations
     * during application startup. Manual configuration is usually not required.
     *
     * <p><strong>Thread Safety:</strong> This method is not thread-safe and should
     * only be called during application initialization before any events are logged.
     *
     * @param processor the observability processor to use for event processing,
     *                  or null to disable event processing
     * @see ObservabilityProcessor
     */
    public static void setProcessor(ObservabilityProcessor processor) {
        Guard4j.processor = processor;
    }

    /**
     * Logs an observability event asynchronously.
     *
     * <p>Events are processed asynchronously to minimize impact on application
     * performance. If no processor is configured, the event is silently ignored.
     *
     * <p>Context information (user ID, request path, etc.) is automatically
     * captured by the processor based on the current execution context.
     *
     * @param event the event to log, must not be null
     * @throws NullPointerException if event is null
     *
     * @see BusinessEvent for business domain events
     * @see ErrorEvent for error-related events
     *
     * @example
     * <pre>{@code
     * // Business event
     * Guard4j.log(new UserRegisteredEvent(userId, registrationSource));
     *
     * // Custom event with metrics tags
     * Guard4j.log(new PaymentProcessedEvent(amount, currency, gateway));
     * }</pre>
     */
    public static void log(ObservableEvent event) {
        if (event == null) {
            throw new NullPointerException("Event cannot be null");
        }

        if (processor != null) {
            processor.processAsync(event);
        }
    }

    /**
     * Returns whether Guard4j observability is currently active.
     *
     * <p>This can be used to conditionally create expensive event objects
     * only when observability is enabled.
     *
     * @return true if a processor is configured and events will be processed
     *
     * @example
     * <pre>{@code
     * if (Guard4j.isActive()) {
     *     // Only create expensive event object if it will be processed
     *     Guard4j.log(new ExpensiveAnalyticsEvent(computeComplexMetrics()));
     * }
     * }</pre>
     */
    public static boolean isActive() {
        return processor != null;
    }
}
