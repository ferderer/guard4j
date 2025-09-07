package de.ferderer.guard4j;

import de.ferderer.guard4j.observability.ObservableEvent;

/**
 * Emitter interface for logging observability events at different levels.
 *
 * <p>This interface follows SLF4J conventions, providing level-specific methods
 * for emitting observability events. Each method accepts an {@link ObservableEvent}
 * and processes it at the appropriate logging level.
 *
 * <p>Usage example:
 * <pre>{@code
 * public class PaymentService {
 *     private static final Emitter events = Guard4j.getEmitter(PaymentService.class);
 *
 *     public void processPayment(Payment payment) {
 *         events.info(new PaymentProcessedEvent(payment.getId(), payment.getAmount()));
 *     }
 * }
 * }</pre>
 *
 * @since 2.0.0
 */
public interface Emitter {

    /**
     * Emit an event at TRACE level.
     *
     * @param event the event to emit, must not be null
     */
    void trace(ObservableEvent event);

    /**
     * Emit an event at DEBUG level.
     *
     * @param event the event to emit, must not be null
     */
    void debug(ObservableEvent event);

    /**
     * Emit an event at INFO level.
     *
     * @param event the event to emit, must not be null
     */
    void info(ObservableEvent event);

    /**
     * Emit an event at WARN level.
     *
     * @param event the event to emit, must not be null
     */
    void warn(ObservableEvent event);

    /**
     * Emit an event at ERROR level.
     *
     * @param event the event to emit, must not be null
     */
    void error(ObservableEvent event);
}
