package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class EventConfigTest {

    @Test
    void shouldDeriveEventTypeFromEnumName() {
        TestEnumEventConfig userRegistered = TestEnumEventConfig.USER_REGISTERED;
        TestEnumEventConfig orderPlaced = TestEnumEventConfig.ORDER_PLACED;
        TestEnumEventConfig paymentProcessed = TestEnumEventConfig.PAYMENT_PROCESSED;
        TestEnumEventConfig singleWord = TestEnumEventConfig.ERROR;

        assertThat(userRegistered.eventType()).isEqualTo("user-registered");
        assertThat(orderPlaced.eventType()).isEqualTo("order-placed");
        assertThat(paymentProcessed.eventType()).isEqualTo("payment-processed");
        assertThat(singleWord.eventType()).isEqualTo("error");
    }

    @Test
    void shouldFallbackToClassNameForNonEnum() {
        TestClassEventConfig classConfig = new TestClassEventConfig();

        assertThat(classConfig.eventType()).isEqualTo("testclasseventconfig");
    }

    @Test
    void shouldHandleComplexEnumNames() {
        TestEnumEventConfig complexName = TestEnumEventConfig.VERY_LONG_EVENT_NAME_WITH_MANY_UNDERSCORES;

        assertThat(complexName.eventType()).isEqualTo("very-long-event-name-with-many-underscores");
    }

    @Test
    void shouldHandleEnumWithNoUnderscores() {
        TestEnumEventConfig simple = TestEnumEventConfig.SIMPLE;

        assertThat(simple.eventType()).isEqualTo("simple");
    }

    @Test
    void shouldFallbackGracefullyForNonEnumClasses() {
        CustomNamedEventConfig customNamed = new CustomNamedEventConfig();

        assertThat(customNamed.eventType()).isEqualTo("customnamedeventconfig");
    }

    // Test enum implementation
    private enum TestEnumEventConfig implements EventConfig {
        USER_REGISTERED(Level.INFO, true),
        ORDER_PLACED(Level.INFO, true),
        PAYMENT_PROCESSED(Level.WARN, true),
        ERROR(Level.ERROR, true),
        SIMPLE(Level.DEBUG, false),
        VERY_LONG_EVENT_NAME_WITH_MANY_UNDERSCORES(Level.INFO, true);

        private final Level level;
        private final boolean hasMetrics;

        TestEnumEventConfig(Level level, boolean hasMetrics) {
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

    // Test class implementation (non-enum)
    private static class TestClassEventConfig implements EventConfig {
        @Override
        public Level level() {
            return Level.INFO;
        }

        @Override
        public boolean hasMetrics() {
            return true;
        }
    }

    // Another test class with different name
    private static class CustomNamedEventConfig implements EventConfig {
        @Override
        public Level level() {
            return Level.WARN;
        }

        @Override
        public boolean hasMetrics() {
            return false;
        }
    }
}
