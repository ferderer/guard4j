package de.ferderer.guard4j.integration;

import de.ferderer.guard4j.Emitter;
import de.ferderer.guard4j.Guard4j;
import de.ferderer.guard4j.observability.ObservableEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Integration test demonstrating the new Emitter pattern usage.
 *
 * This test shows how the new Guard4j API should be used in application code.
 */
public class EmitterPatternExampleIT {

    @Test
    void demonstrateNewEmitterPattern() {
        // Simple event definition - just implement ObservableEvent
        record PaymentProcessedEvent(String paymentId, BigDecimal amount) implements ObservableEvent {}

        record UserRegisteredEvent(String userId, String userType) implements ObservableEvent {
            @Override
            public int metric() {
                // Custom metric value - e.g., different weights for different user types
                return "premium".equals(userType) ? 5 : 1;
            }
        }

        // Service class using the new Emitter pattern
        class PaymentService {
            private static final Emitter events = Guard4j.getEmitter(PaymentService.class);

            public void processPayment(String paymentId, BigDecimal amount) {
                // Emit business event at INFO level
                events.info(new PaymentProcessedEvent(paymentId, amount));
            }

            public void handlePaymentFailure(String paymentId, String error) {
                // Emit error event at ERROR level
                events.error(new PaymentProcessedEvent(paymentId, BigDecimal.ZERO));
            }
        }

        class UserService {
            private static final Emitter events = Guard4j.getEmitter(UserService.class);

            public void registerUser(String userId, String userType) {
                // Emit event with custom metric value
                events.info(new UserRegisteredEvent(userId, userType));
            }
        }

        // Usage demonstration
        var paymentService = new PaymentService();
        var userService = new UserService();

        // These calls would normally generate:
        // - Metrics: incremented by 1 (payment) and 5 (premium user)
        // - Logs: written to PaymentService and UserService loggers at INFO level
        paymentService.processPayment("pay-123", new BigDecimal("99.99"));
        userService.registerUser("user-456", "premium");
    }
}
