package de.ferderer.guard4j;

/**
 * Main entry point for Guard4j observability using the Emitter pattern.
 *
 * <p>Guard4j provides a unified API for emitting observability events
 * across different Java frameworks. This class serves as the primary interface for
 * applications to obtain emitters for event emission.
 *
 * <h3>Setup</h3>
 * <p>Framework integrations automatically configure the processor during application startup:
 * <pre>{@code
 * // Automatic setup via Spring Boot starter, Quarkus extension, etc.
 * // No manual configuration required
 * }</pre>
 *
 * <h3>Emitting Events</h3>
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
 * <h3>Event Definition</h3>
 * <pre>{@code
 * public record PaymentProcessedEvent(String paymentId, BigDecimal amount)
 *     implements ObservableEvent {}
 * }</pre>
 *
 * <h3>Framework Integration</h3>
 * <p>This class is automatically configured by:
 * <ul>
 *   <li>Spring Boot: {@code guard4j-spring-boot-starter}</li>
 *   <li>Quarkus: {@code guard4j-quarkus}</li>
 *   <li>Micronaut: {@code guard4j-micronaut}</li>
 * </ul>
 *
 * @since 2.0.0
 */
public final class Guard4j {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Guard4j() {}

    /**
     * Gets an emitter for the specified class.
     *
     * <p>The emitter provides SLF4J-style logging methods (trace, debug, info, warn, error)
     * for emitting observability events. Events are processed asynchronously to minimize
     * impact on application performance.
     *
     * <p>If no processor is configured, events are silently ignored.
     *
     * @param clazz the class for which to get an emitter
     * @return an emitter instance for the specified class, never null
     */
    public static Emitter getEmitter(Class<?> clazz) {
        return EmitterFactory.getEmitter(clazz.getName());
    }
}
